package com.example.focuslauncher.domain.focus

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Weekly schedule for auto-starting a focus profile.
 * Serialized as `{"days":[1,2,3],"start":540,"end":720}` where days use
 * [DayOfWeek] numbers (Monday=1) and start/end are minutes since midnight.
 * Hand-rolled format: no Android dependencies, JVM-testable.
 */
data class FocusSchedule(
    val days: Set<Int> = emptySet(),
    val startMin: Int = 9 * 60,
    val endMin: Int = 12 * 60,
) {
    /** True when [now] falls inside the window (overnight windows supported). */
    fun isActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (days.isEmpty() || startMin == endMin) return false
        val day = now.dayOfWeek.value
        val min = now.hour * 60 + now.minute
        return if (startMin < endMin) {
            day in days && min in startMin until endMin
        } else {
            // Overnight, e.g. 22:00-06:00: evening of a listed day, or early
            // morning whose previous day is listed.
            (day in days && min >= startMin) ||
                (previousDay(day) in days && min < endMin)
        }
    }

    fun toJson(): String =
        """{"days":[${days.sorted().joinToString(",")}],"start":$startMin,"end":$endMin}"""

    companion object {
        fun fromJson(json: String?): FocusSchedule? {
            if (json.isNullOrBlank()) return null
            return try {
                val days = Regex("\"days\"\\s*:\\s*\\[([^]]*)]")
                    .find(json)?.groupValues?.get(1)
                    ?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
                    ?.filter { it in 1..7 }?.toSet() ?: return null
                val start = Regex("\"start\"\\s*:\\s*(\\d+)")
                    .find(json)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                val end = Regex("\"end\"\\s*:\\s*(\\d+)")
                    .find(json)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                if (start !in 0..1439 || end !in 0..1439) return null
                FocusSchedule(days, start, end)
            } catch (_: Exception) {
                null
            }
        }

        fun formatTime(min: Int): String =
            "%02d:%02d".format(min / 60, min % 60)

        fun parseTime(text: String): Int? {
            val m = Regex("^(\\d{1,2}):(\\d{2})$").matchEntire(text.trim()) ?: return null
            val h = m.groupValues[1].toInt()
            val mm = m.groupValues[2].toInt()
            if (h !in 0..23 || mm !in 0..59) return null
            return h * 60 + mm
        }

        private fun previousDay(day: Int): Int = if (day == 1) 7 else day - 1
    }
}
