package com.example.focuslauncher.data.focus

import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.focus.isExpired
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the current focus session. The open session row is the source of
 * truth, so state survives process death: [refresh] (called on init and on
 * boot) re-emits the still-running session or closes an expired one.
 */
@Singleton
class FocusManager @Inject constructor(
    private val sessions: FocusSessionDao,
    private val profiles: FocusProfileDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<FocusState>(FocusState.Idle)
    val state: StateFlow<FocusState> = _state.asStateFlow()

    init {
        scope.launch { refresh() }
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
            FocusState.Active(id, profileId, now, endAt).also { _state.value = it }
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
    }

    /**
     * Reconcile in-memory state with the DB. Call on app start and on boot:
     * resumes a running session, closes an expired one as completed.
     */
    suspend fun refresh(): Unit = withContext(Dispatchers.IO) {
        val open = sessions.openSession() ?: run {
            _state.value = FocusState.Idle
            return@withContext
        }
        val now = System.currentTimeMillis()
        val active = FocusState.Active(open.id, open.profileId, open.startAt, open.endAt)
        if (active.isExpired(now)) {
            sessions.close(open.id, open.endAt, false, null)
            _state.value = FocusState.Idle
        } else {
            _state.value = active
        }
    }

    /** Create a profile; returns its id. Used by onboarding and settings (3.2). */
    suspend fun createProfile(name: String): Long = withContext(Dispatchers.IO) {
        profiles.insert(FocusProfile(name = name))
    }
}
