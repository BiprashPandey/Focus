package com.example.focuslauncher.data.focus

import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.FocusFilter
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.focus.isExpired
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the current focus session. The open session row is the source of
 * truth, so state survives process death: [refresh] (called on init and on
 * boot) re-emits the still-running session or closes an expired one.
 *
 * Starting a session also schedules the end notification and applies the
 * profile's DND mode; stopping restores everything. [appContext] is nullable
 * so unit tests can run without Android.
 */
@Singleton
class FocusManager @Inject constructor(
    private val sessions: FocusSessionDao,
    private val profiles: FocusProfileDao,
    private val dnd: DndController = DndController(),
    @ApplicationContext private val appContext: Context? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<FocusState>(FocusState.Idle)
    val state: StateFlow<FocusState> = _state.asStateFlow()

    val allProfiles: Flow<List<FocusProfile>> = profiles.observeAll()

    init {
        scope.launch {
            ensureDefaults()
            refresh()
            ensureScheduleWorker()
        }
    }

    /** Start a session for [profileId] lasting [durationMin] (25/50/90/custom). */
    suspend fun start(profileId: Long, durationMin: Int): FocusState.Active =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            // Close any stale open session before starting a new one.
            sessions.openSession()?.let { open ->
                sessions.close(open.id, now, true, "superseded")
            }
            val endAt = FocusDurations.endsAt(now, durationMin)
            val id = sessions.insert(
                FocusSession(
                    profileId = profileId,
                    startAt = now,
                    endAt = endAt,
                    plannedMin = durationMin,
                ),
            )
            val active = FocusState.Active(id, profileId, now, endAt)
            _state.value = active
            profiles.get(profileId)?.let { applyProfileSideEffects(it, durationMin) }
            active
        }

    /** End the session now; records whether it ended early and an optional reason. */
    suspend fun stop(exitReason: String? = null): Unit = withContext(Dispatchers.IO) {
        val current = _state.value as? FocusState.Active
            ?: sessions.openSession()?.let { open ->
                FocusState.Active(open.id, open.profileId, open.startAt, open.endAt)
            }
            ?: return@withContext
        val now = System.currentTimeMillis()
        sessions.close(current.sessionId, now, now < current.endsAt, exitReason)
        _state.value = FocusState.Idle
        clearSideEffects()
    }

    /**
     * Reconcile in-memory state with the DB. Call on app start and on boot:
     * resumes a running session, closes an expired one as completed.
     */
    suspend fun refresh(): Unit = withContext(Dispatchers.IO) {
        val open = sessions.openSession() ?: run {
            _state.value = FocusState.Idle
            clearSideEffects()
            return@withContext
        }
        val now = System.currentTimeMillis()
        val active = FocusState.Active(open.id, open.profileId, open.startAt, open.endAt)
        if (active.isExpired(now)) {
            sessions.close(open.id, open.endAt, false, null)
            _state.value = FocusState.Idle
            clearSideEffects()
        } else {
            _state.value = active
        }
    }

    /** Create a profile; returns its id. Used by onboarding and settings. */
    suspend fun createProfile(name: String): Long = withContext(Dispatchers.IO) {
        profiles.insert(FocusProfile(name = name))
    }

    suspend fun setAllowedApps(profileId: Long, packages: Set<String>) {
        withContext(Dispatchers.IO) {
            profiles.updateAllowed(profileId, FocusFilter.encodeAllowed(packages))
        }
    }

    suspend fun setDnd(profileId: Long, enabled: Boolean) {
        withContext(Dispatchers.IO) {
            profiles.updateDnd(profileId, enabled)
        }
    }

    suspend fun setSchedule(profileId: Long, json: String?) {
        withContext(Dispatchers.IO) {
            profiles.updateSchedule(profileId, json)
        }
    }

    private suspend fun ensureDefaults() {
        if (profiles.count() == 0) {
            profiles.insert(FocusProfile(name = "Work"))
            profiles.insert(FocusProfile(name = "Study"))
            profiles.insert(FocusProfile(name = "Wind-down"))
        }
    }

    private fun applyProfileSideEffects(profile: FocusProfile, durationMin: Int) {
        val ctx = appContext ?: return
        if (profile.dndEnabled) {
            ctx.getSystemService(NotificationManager::class.java)?.let { dnd.enable(it) }
        }
        val work = OneTimeWorkRequestBuilder<FocusEndWorker>()
            .setInitialDelay(durationMin.toLong(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    FocusEndWorker.KEY_PROFILE to profile.name,
                    FocusEndWorker.KEY_MINUTES to durationMin,
                ),
            )
            .build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(
            END_WORK,
            ExistingWorkPolicy.REPLACE,
            work,
        )
    }

    private fun clearSideEffects() {
        val ctx = appContext ?: return
        WorkManager.getInstance(ctx).cancelUniqueWork(END_WORK)
        ctx.getSystemService(NotificationManager::class.java)?.let { dnd.restore(it) }
    }

    private fun ensureScheduleWorker() {
        val ctx = appContext ?: return
        val work = PeriodicWorkRequestBuilder<ScheduleCheckWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            ScheduleCheckWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            work,
        )
    }

    companion object {
        private const val END_WORK = "focus_end"
    }
}
