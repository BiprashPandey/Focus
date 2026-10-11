package com.example.focuslauncher.data.theme

/** Serializable theme configuration (DataStore + future export/import). */
data class ThemeConfig(
    val mode: String = ThemeModes.SYSTEM,
    val colorSource: String = ColorSources.SYSTEM,
    /** ARGB seed for custom/wallpaper palettes. */
    val seedColor: Long = ThemePresets.AURORA.seed,
    /** Last wallpaper-extracted seed, if any. */
    val wallpaperSeed: Long? = null,
    val cornerStyle: String = CornerStyles.ROUNDED,
    /** App icon size in dp (40..72). */
    val iconSizeDp: Int = 56,
)

object ThemeModes {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
    const val AMOLED = "amoled"
    val all = listOf(SYSTEM, LIGHT, DARK, AMOLED)
}

object ColorSources {
    const val WALLPAPER = "wallpaper"
    const val SYSTEM = "system"
    const val CUSTOM = "custom"
    val all = listOf(WALLPAPER, SYSTEM, CUSTOM)
}

object CornerStyles {
    const val SHARP = "sharp"
    const val ROUNDED = "rounded"
    const val EXTRA = "extra"
    val all = listOf(SHARP, ROUNDED, EXTRA)

    fun multiplier(style: String): Double = when (style) {
        SHARP -> 0.5
        EXTRA -> 1.5
        else -> 1.0
    }
}

data class ThemePreset(val name: String, val seed: Long, val amoled: Boolean = false)

object ThemePresets {
    val AURORA = ThemePreset("Aurora", 0xFF6C63FF)
    val all = listOf(
        AURORA,
        ThemePreset("Ocean", 0xFF1E88E5),
        ThemePreset("Forest", 0xFF43A047),
        ThemePreset("Sunset", 0xFFFF7043),
        ThemePreset("Rose", 0xFFE91E63),
        ThemePreset("Mono", 0xFF9E9E9E),
        ThemePreset("Midnight", 0xFF0D1B2A, amoled = true),
        ThemePreset("Sand", 0xFFC8A97E),
    )
}
