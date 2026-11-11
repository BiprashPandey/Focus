package com.example.focuslauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Accessible accent pair for a single home module. */
data class AccentColors(
    /** Tinted container used behind the module icon and highlights. */
    val container: Color,
    /** Readable foreground on [container] and tinted glass. */
    val content: Color,
    /** Saturated hue used for rings, dots and gradient buttons. */
    val bright: Color,
)

/**
 * Semantic module colors. These stay vivid regardless of the seed palette,
 * while the rest of the chrome still follows Material You.
 */
enum class Accent(val light: AccentColors, val dark: AccentColors) {
    FOCUS(
        light = AccentColors(
            container = Color(0xFFEDE8FF),
            content = Color(0xFF31246E),
            bright = Color(0xFF7C6CFF),
        ),
        dark = AccentColors(
            container = Color(0xFF2E2757),
            content = Color(0xFFE8E2FF),
            bright = Color(0xFFB9ADFF),
        ),
    ),
    TODOS(
        light = AccentColors(
            container = Color(0xFFFFF1D6),
            content = Color(0xFF5C4200),
            bright = Color(0xFFF2A93B),
        ),
        dark = AccentColors(
            container = Color(0xFF3D2E10),
            content = Color(0xFFFFE3A8),
            bright = Color(0xFFFFC65C),
        ),
    ),
    NOTES(
        light = AccentColors(
            container = Color(0xFFDDF7F1),
            content = Color(0xFF0B4F45),
            bright = Color(0xFF19B79B),
        ),
        dark = AccentColors(
            container = Color(0xFF0F3B35),
            content = Color(0xFFB8F5E9),
            bright = Color(0xFF4FD8C0),
        ),
    ),
    CALENDAR(
        light = AccentColors(
            container = Color(0xFFE2EFFF),
            content = Color(0xFF123A66),
            bright = Color(0xFF3B82F6),
        ),
        dark = AccentColors(
            container = Color(0xFF142C4D),
            content = Color(0xFFC9E1FF),
            bright = Color(0xFF7CB8FF),
        ),
    ),
    MOTIVATION(
        light = AccentColors(
            container = Color(0xFFFFE7EC),
            content = Color(0xFF6B1730),
            bright = Color(0xFFF25C78),
        ),
        dark = AccentColors(
            container = Color(0xFF471E2A),
            content = Color(0xFFFFD9E1),
            bright = Color(0xFFFF8FA3),
        ),
    ),
}

@Composable
fun Accent.colors(): AccentColors = if (LocalAuroraIsDark.current) dark else light
