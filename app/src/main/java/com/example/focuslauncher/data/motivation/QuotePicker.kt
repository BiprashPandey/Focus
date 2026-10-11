package com.example.focuslauncher.data.motivation

/** One bundled quote. `tone` is `general` or one of the Tones values. */
data class Quote(val text: String, val by: String, val tone: String)

/**
 * Pure-Kotlin quote picking over the bundled library. Hand-rolled parser so
 * the logic stays JVM-testable (no Android JSON stubs).
 */
object QuotePicker {

    private val ENTRY = Regex("\\{\\s*\"t\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"b\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"tone\"\\s*:\\s*\"(.*?)\"\\s*\\}")

    fun parse(json: String): List<Quote> = ENTRY.findAll(json).map { m ->
        Quote(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }.toList()

    /**
     * Deterministic pick per day: tone-specific entries when they exist,
     * otherwise the shared `general` pool. Rotates daily.
     */
    fun pick(quotes: List<Quote>, tone: String, epochDay: Long): Quote? {
        if (quotes.isEmpty()) return null
        val pool = quotes.filter { it.tone == tone }.ifEmpty {
            quotes.filter { it.tone == "general" }
        }.ifEmpty { quotes }
        val index = (epochDay % pool.size).toInt().let { if (it < 0) it + pool.size else it }
        return pool[index]
    }
}

/** Cap of 3 proactive messages/day, silent during quiet hours (22:00-07:00). */
object NudgePolicy {
    const val MAX_PER_DAY = 3

    fun shouldShow(servedToday: Int, hour: Int): Boolean {
        if (servedToday >= MAX_PER_DAY) return false
        if (hour >= 22 || hour < 7) return false
        return true
    }
}
