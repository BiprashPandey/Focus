package com.example.focuslauncher.domain.focus

/** Pure-Kotlin focus session state. No Android types, unit-testable on the JVM. */
sealed interface FocusState {
    data object Idle : FocusState

    data class Active(
        val sessionId: Long,
        val profileId: Long,
        val startedAt: Long,
        val endsAt: Long,
    ) : FocusState
}

/** Preset durations plus free-form custom minutes. */
object FocusDurations {
    const val POMODORO = 25
    const val DEEP_WORK = 50
    const val LONG = 90
    val presets = listOf(POMODORO, DEEP_WORK, LONG)

    fun endsAt(startedAt: Long, durationMin: Int): Long {
        require(durationMin > 0) { "duration must be positive minutes" }
        return startedAt + durationMin * 60_000L
    }
}

/** Milliseconds left in an [FocusState.Active] session, never negative. */
fun FocusState.Active.remaining(now: Long): Long = (endsAt - now).coerceAtLeast(0L)

/** True when the session end time has passed. */
fun FocusState.Active.isExpired(now: Long): Boolean = now >= endsAt

/** "24:59" style countdown for the timer card. */
fun FocusState.Active.countdown(now: Long): String {
    val totalSec = (remaining(now) / 1000).toInt()
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}
