package com.example.focuslauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.data.theme.ColorSources
import com.example.focuslauncher.data.theme.CornerStyles
import com.example.focuslauncher.data.theme.SeedScheme
import com.example.focuslauncher.data.theme.ThemeConfig
import com.example.focuslauncher.data.theme.ThemeModes

private val FallbackDark = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val FallbackLight = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

@Composable
fun FocusLauncherTheme(
    config: ThemeConfig = ThemeConfig(),
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val dark = when (config.mode) {
        ThemeModes.LIGHT -> false
        ThemeModes.DARK, ThemeModes.AMOLED -> true
        else -> systemDark
    }
    val context = LocalContext.current
    val colorScheme: ColorScheme = remember(config, dark, context) {
        when {
            config.mode == ThemeModes.AMOLED -> SeedScheme.amoled(effectiveSeed(config).toInt())
            config.colorSource == ColorSources.SYSTEM &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            config.colorSource == ColorSources.SYSTEM -> {
                if (dark) FallbackDark else FallbackLight
            }
            else -> SeedScheme.scheme(effectiveSeed(config).toInt(), dark)
        }
    }
    val scale = remember(config.cornerStyle) {
        CornerStyles.multiplier(config.cornerStyle)
    }
    val shapes = remember(scale) {
        Shapes(
            extraSmall = RoundedCornerShape((8 * scale).dp),
            small = RoundedCornerShape((12 * scale).dp),
            medium = RoundedCornerShape((16 * scale).dp),
            large = RoundedCornerShape((24 * scale).dp),
            extraLarge = RoundedCornerShape((32 * scale).dp),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content,
    )
}

/** Wallpaper source uses the last extracted seed, falling back to the custom seed. */
private fun effectiveSeed(config: ThemeConfig): Long = when (config.colorSource) {
    ColorSources.WALLPAPER -> config.wallpaperSeed ?: config.seedColor
    else -> config.seedColor
}
