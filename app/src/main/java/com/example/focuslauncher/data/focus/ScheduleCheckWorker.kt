package com.example.focuslauncher.data.focus

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.focuslauncher.data.appconfig.FocusDatabase
import com.example.focuslauncher.data.appconfig.MIGRATION_1_2
import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.FocusSchedule
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * Plain worker (no Hilt): every 15 minutes, start a session for the first
 * profile whose schedule window is active and has no open session. Uses its
 * own Room instance; the app process reconciles via FocusManager.refresh().
 * Schedules recompute on every run, so reboot/timezone changes just work.
 */
class ScheduleCheckWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(
            applicationContext,
            FocusDatabase::class.java,
            "focus.db",
        ).addMigrations(MIGRATION_1_2).build()
        try {
            if (db.focusSessionDao().openSession() != null) return Result.success()
            val now = LocalDateTime.now()
            val profiles: List<FocusProfile> = db.focusProfileDao().observeAll().first()
            val match = profiles
                .firstOrNull { FocusSchedule.fromJson(it.scheduleJson)?.isActive(now) == true }
                ?: return Result.success()
            val schedule = FocusSchedule.fromJson(match.scheduleJson) ?: return Result.success()
            val minutes = ((schedule.endMin - schedule.startMin).let { if (it <= 0) it + 1440 else it })
                .coerceIn(5, 240)
            val startAt = System.currentTimeMillis()
            db.focusSessionDao().insert(
                FocusSession(
                    profileId = match.id,
                    startAt = startAt,
                    endAt = FocusDurations.endsAt(startAt, minutes),
                    plannedMin = minutes,
                ),
            )
            return Result.success()
        } finally {
            db.close()
        }
    }

    companion object {
        const val UNIQUE_NAME = "focus_schedule"
    }
}
