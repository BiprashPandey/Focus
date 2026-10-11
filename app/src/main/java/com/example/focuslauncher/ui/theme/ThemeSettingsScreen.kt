package com.example.focuslauncher.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.theme.ColorSources
import com.example.focuslauncher.data.theme.CornerStyles
import com.example.focuslauncher.data.theme.ThemeConfig
import com.example.focuslauncher.data.theme.ThemeModes
import com.example.focuslauncher.data.theme.ThemePreset
import com.example.focuslauncher.data.theme.ThemePresets
import com.example.focuslauncher.ui.components.SettingsGroup
import kotlin.math.roundToInt

@Composable
fun ThemeSettingsScreen(
    config: ThemeConfig,
    onMode: (String) -> Unit,
    onSource: (String) -> Unit,
    onPreset: (ThemePreset) -> Unit,
    onSeedHex: (Long) -> Unit,
    onCorner: (String) -> Unit,
    onIconSize: (Int) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.theme_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.manage_apps_close),
                        )
                    }
                }
            }
            item(key = "preview") {
                ThemePreviewCard()
            }
            item(key = "mode") {
                SettingsGroup(title = stringResource(R.string.theme_mode)) {
                    ChipFlow(
                        options = listOf(
                            ThemeModes.SYSTEM to stringResource(R.string.theme_mode_system),
                            ThemeModes.LIGHT to stringResource(R.string.theme_mode_light),
                            ThemeModes.DARK to stringResource(R.string.theme_mode_dark),
                            ThemeModes.AMOLED to stringResource(R.string.theme_mode_amoled),
                        ),
                        selected = config.mode,
                        onSelect = onMode,
                    )
                }
            }
            item(key = "source") {
                SettingsGroup(title = stringResource(R.string.theme_source)) {
                    ChipFlow(
                        options = listOf(
                            ColorSources.WALLPAPER to stringResource(R.string.theme_source_wallpaper),
                            ColorSources.SYSTEM to stringResource(R.string.theme_source_system),
                            ColorSources.CUSTOM to stringResource(R.string.theme_source_custom),
                        ),
                        selected = config.colorSource,
                        onSelect = onSource,
                    )
                    Text(
                        text = stringResource(R.string.theme_source_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    )
                }
            }
            item(key = "presets") {
                SettingsGroup(title = stringResource(R.string.theme_presets)) {
                    LazyRow(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(ThemePresets.all, key = { it.name }) { preset ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val selected = config.seedColor == preset.seed &&
                                    config.colorSource == ColorSources.CUSTOM
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.seed.toULong()))
                                        .then(
                                            if (selected) {
                                                Modifier.border(
                                                    3.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .clickable { onPreset(preset) },
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
            item(key = "seed") {
                var hex by remember(config.seedColor) {
                    mutableStateOf("%06X".format(config.seedColor.toULong() and 0xFFFFFFu))
                }
                SettingsGroup(title = stringResource(R.string.theme_custom_seed)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(config.seedColor.toULong())),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = hex,
                            onValueChange = {
                                hex = it.filter { c ->
                                    c.isDigit() || c in 'a'..'f' || c in 'A'..'F'
                                }.take(6)
                            },
                            label = { Text(stringResource(R.string.theme_hex)) },
                            prefix = { Text("#") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                hex.toLongOrNull(16)?.let { onSeedHex(0xFF000000L or it) }
                            },
                            enabled = hex.length == 6,
                        ) {
                            Text(stringResource(R.string.theme_apply))
                        }
                    }
                }
            }
            item(key = "corners") {
                SettingsGroup(title = stringResource(R.string.theme_corners)) {
                    ChipFlow(
                        options = listOf(
                            CornerStyles.SHARP to stringResource(R.string.theme_corner_sharp),
                            CornerStyles.ROUNDED to stringResource(R.string.theme_corner_rounded),
                            CornerStyles.EXTRA to stringResource(R.string.theme_corner_extra),
                        ),
                        selected = config.cornerStyle,
                        onSelect = onCorner,
                    )
                }
            }
            item(key = "icons") {
                var slider by remember(config.iconSizeDp) {
                    mutableFloatStateOf(config.iconSizeDp.toFloat())
                }
                SettingsGroup(title = stringResource(R.string.theme_icon_size, config.iconSizeDp)) {
                    Slider(
                        value = slider,
                        onValueChange = { slider = it },
                        onValueChangeFinished = { onIconSize(slider.roundToInt()) },
                        valueRange = 40f..72f,
                        steps = 31,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            item(key = "reset") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onReset) {
                        Text(stringResource(R.string.theme_reset))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChipFlow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (value, label) ->
                    FilterChip(
                        shape = CircleShape,
                        selected = selected == value,
                        onClick = { onSelect(value) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}

/** Live miniature preview reflecting the active scheme and shapes. */
@Composable
private fun ThemePreviewCard() {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.theme_preview),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.swipe_up_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = {}) {
                    Text(stringResource(R.string.focus_start))
                }
            }
        }
    }
}
