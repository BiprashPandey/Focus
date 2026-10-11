package com.example.focuslauncher.data.theme

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

/**
 * Seed-color palettes via the Material Color Utilities vendored in the
 * Material library (Tonal Spot, the same family Material 3 baseline uses).
 * Pure JVM logic, unit-tested.
 *
 * Uses restricted MCU internals (the same calls dynamic color makes):
 * the Material dependency is version-pinned, so this is stable in practice.
 */
@SuppressLint("RestrictedApi")
object SeedScheme {

    private val dynamics = MaterialDynamicColors()

    @SuppressLint("RestrictedApi")
    private fun role(
        scheme: DynamicScheme,
        get: MaterialDynamicColors.() -> DynamicColor,
    ): Color = Color(dynamics.get().getArgb(scheme))

    @SuppressLint("RestrictedApi")
    fun scheme(seedArgb: Int, dark: Boolean): ColorScheme {
        val scheme: DynamicScheme = SchemeTonalSpot(Hct.fromInt(seedArgb), dark, 0.0)
        return if (dark) {
            darkColorScheme(
                primary = role(scheme) { primary() },
                onPrimary = role(scheme) { onPrimary() },
                primaryContainer = role(scheme) { primaryContainer() },
                onPrimaryContainer = role(scheme) { onPrimaryContainer() },
                secondary = role(scheme) { secondary() },
                onSecondary = role(scheme) { onSecondary() },
                secondaryContainer = role(scheme) { secondaryContainer() },
                onSecondaryContainer = role(scheme) { onSecondaryContainer() },
                tertiary = role(scheme) { tertiary() },
                onTertiary = role(scheme) { onTertiary() },
                tertiaryContainer = role(scheme) { tertiaryContainer() },
                onTertiaryContainer = role(scheme) { onTertiaryContainer() },
                background = role(scheme) { background() },
                onBackground = role(scheme) { onBackground() },
                surface = role(scheme) { surface() },
                onSurface = role(scheme) { onSurface() },
                surfaceVariant = role(scheme) { surfaceVariant() },
                onSurfaceVariant = role(scheme) { onSurfaceVariant() },
                outline = role(scheme) { outline() },
                outlineVariant = role(scheme) { outlineVariant() },
                scrim = role(scheme) { scrim() },
                error = role(scheme) { error() },
                onError = role(scheme) { onError() },
            )
        } else {
            lightColorScheme(
                primary = role(scheme) { primary() },
                onPrimary = role(scheme) { onPrimary() },
                primaryContainer = role(scheme) { primaryContainer() },
                onPrimaryContainer = role(scheme) { onPrimaryContainer() },
                secondary = role(scheme) { secondary() },
                onSecondary = role(scheme) { onSecondary() },
                secondaryContainer = role(scheme) { secondaryContainer() },
                onSecondaryContainer = role(scheme) { onSecondaryContainer() },
                tertiary = role(scheme) { tertiary() },
                onTertiary = role(scheme) { onTertiary() },
                tertiaryContainer = role(scheme) { tertiaryContainer() },
                onTertiaryContainer = role(scheme) { onTertiaryContainer() },
                background = role(scheme) { background() },
                onBackground = role(scheme) { onBackground() },
                surface = role(scheme) { surface() },
                onSurface = role(scheme) { onSurface() },
                surfaceVariant = role(scheme) { surfaceVariant() },
                onSurfaceVariant = role(scheme) { onSurfaceVariant() },
                outline = role(scheme) { outline() },
                outlineVariant = role(scheme) { outlineVariant() },
                scrim = role(scheme) { scrim() },
                error = role(scheme) { error() },
                onError = role(scheme) { onError() },
            )
        }
    }

    /** AMOLED: true-black background with near-black surfaces. */
    @SuppressLint("RestrictedApi")
    fun amoled(seedArgb: Int): ColorScheme = scheme(seedArgb, dark = true).copy(
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surfaceVariant = Color(0xFF121212),
    )
}
