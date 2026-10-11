package com.example.focuslauncher.data.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedSchemeTest {

    @Test
    fun differentSeedsGiveDifferentPrimaries() {
        val violet = SeedScheme.scheme(0xFF6C63FF.toInt(), dark = false)
        val green = SeedScheme.scheme(0xFF43A047.toInt(), dark = false)
        assertNotEquals(violet.primary, green.primary)
    }

    @Test
    fun darkAndLightDiffer() {
        val seed = 0xFF1E88E5.toInt()
        val light = SeedScheme.scheme(seed, dark = false)
        val dark = SeedScheme.scheme(seed, dark = true)
        assertNotEquals(light.background, dark.background)
    }

    @Test
    fun amoledUsesTrueBlack() {
        val amoled = SeedScheme.amoled(0xFF0D1B2A.toInt())
        assertEquals(0xFF000000.toInt(), amoled.background.toArgb())
    }

    @Test
    fun cornerMultipliers() {
        assertEquals(0.5, CornerStyles.multiplier(CornerStyles.SHARP), 0.0)
        assertEquals(1.0, CornerStyles.multiplier(CornerStyles.ROUNDED), 0.0)
        assertEquals(1.5, CornerStyles.multiplier(CornerStyles.EXTRA), 0.0)
    }

    @Test
    fun presetsCoverEightThemes() {
        assertEquals(8, ThemePresets.all.size)
        assertTrue(ThemePresets.all.any { it.amoled })
    }
}
