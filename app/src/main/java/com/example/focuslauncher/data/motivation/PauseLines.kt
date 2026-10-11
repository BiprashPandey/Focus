package com.example.focuslauncher.data.motivation

/** Calm, shame-free lines for the pause screen. Original copy, no attribution needed. */
object PauseLines {
    val lines: List<String> = listOf(
        "A short pause can make the next choice clearer.",
        "Breathe in, breathe out. What do you need right now?",
        "Small pauses add up to calmer days.",
        "You have time. One slow breath first.",
        "Notice the urge — it passes quicker than it feels.",
        "A 10-minute reset beats 10 minutes of scrolling.",
        "Gentle check: is this what you came here to do?",
        "Your attention is precious. Spend it on purpose.",
        "Pause first, then decide with a clear head.",
        "One thing at a time. You have got this.",
        "Stretch your shoulders while you wait.",
        "Drink some water. Future you says thanks.",
        "Stepping back for a moment is a strength.",
        "What is one tiny thing that would feel good?",
        "Calm is a choice you can practice.",
        "No rush, no guilt — just a breath.",
        "Today is built one small decision at a time.",
        "Give yourself a moment. Then choose kindly.",
        "Stillness first. The phone will wait.",
        "You are allowed to do something else instead.",
    )

    /** Stable pick per day so the line feels fresh but not random every second. */
    fun forDay(epochDay: Long): String = lines[(epochDay % lines.size).toInt().coerceAtLeast(0)]
}
