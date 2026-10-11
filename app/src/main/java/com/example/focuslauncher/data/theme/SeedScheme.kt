package com.example.focuslauncher.data.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

/**
 * Seed-color palettes via the Material Color Utilities vendored in the
 * Material library (Tonal Spot, the same family Material 3 baseline uses).
 * Pure JVM logic, unit-tested.
 */
object SeedScheme {

    private val dynamics = MaterialDynamicColors()

    fun scheme(seedArgb: Int, dark: Boolean): ColorScheme {
        val scheme: DynamicScheme = SchemeTonalSpot(Hct.fromInt(seedArgb), dark, 0.0)
        fun role(get: MaterialDynamicColors.() -> com.google.android.material.color.utilities.DynamicColor): Color =
            Color(dynamics.get().getArgb(scheme))
        return if (dark) {
            darkColorScheme(
                primary = role { primary() },
                onPrimary = role { onPrimary() },
                primaryContainer = role { primaryContainer() },
                onPrimaryContainer = role { onPrimaryContainer() },
                secondary = role { secondary() },
                onSecondary = role { onSecondary() },
                secondaryContainer = role { secondaryContainer() },
                onSecondaryContainer = role { onSecondaryContainer() },
                tertiary = role { tertiary() },
                onTertiary = role { onTertiary() },
                tertiaryContainer = role { tertiaryContainer() },
                onTertiaryContainer = role { onTertiaryContainer() },
                background = role { background() },
                onBackground = role { onBackground() },
                surface = role { surface() },
                onSurface = role { onSurface() },
                surfaceVariant = role { surfaceVariant() },
                onSurfaceVariant = role { onSurfaceVariant() },
                outline = role { outline() },
                outlineVariant = role { outlineVariant() },
                scrim = role { scrim() },
                error = role { error() },
                onError = role { onError() },
            )
        } else {
            lightColorScheme(
                primary = role { primary() },
                onPrimary = role { onPrimary() },
                primaryContainer = role { primaryContainer() },
                onPrimaryContainer = role { onPrimaryContainer() },
                secondary = role { secondary() },
                onSecondary = role { onSecondary() },
                secondaryContainer = role { secondaryContainer() },
                onSecondaryContainer = role { onSecondaryContainer() },
                tertiary = role { tertiary() },
                onTertiary = role { onTertiary() },
                tertiaryContainer = role { tertiaryContainer() },
                onTertiaryContainer = role { onTertiaryContainer() },
                background = role { background() },
                onBackground = role { onBackground() },
                surface = role { surface() },
                onSurface = role { onSurface() },
                surfaceVariant = role { surfaceVariant() },
                onSurfaceVariant = role { onSurfaceVariant() },
                outline = role { outline() },
                outlineVariant = role { outlineVariant() },
                scrim = role { scrim() },
                error = role { error() },
                onError = role { onError() },
            )
        }
    }

    /** AMOLED: true-black background with near-black surfaces. */
    fun amoled(seedArgb: Int): ColorScheme = scheme(seedArgb, dark = true).copy(
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surfaceVariant = Color(0xFF121212),
    )
}
