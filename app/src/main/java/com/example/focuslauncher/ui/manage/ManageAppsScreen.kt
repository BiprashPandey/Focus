package com.example.focuslauncher.ui.manage

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.EmptyState
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.SearchField
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.drawer.AppIcon
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

@Composable
fun ManageAppsScreen(
    state: ManageAppsUiState,
    onQueryChange: (String) -> Unit,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    onSetLimit: (AppInfo, Int?) -> Unit = { _, _ -> },
    onClose: () -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
) {
    BackHandler { onClose() }
    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.manage_apps_title),
                subtitle = stringResource(R.string.manage_apps_subtitle),
                onClose = onClose,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatPill(
                    text = stringResource(R.string.apps_count, state.rows.size),
                    accent = Accent.NOTES,
                )
            }
            SearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.manage_apps_search),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
            if (state.rows.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.no_apps_found),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.rows, key = { it.app.key }) { row ->
                        ManageRow(
                            row = row,
                            onSetLevel = onSetLevel,
                            onSetLimit = onSetLimit,
                            loadIcon = loadIcon,
                            iconSize = iconSize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageRow(
    row: ManageAppRow,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    onSetLimit: (AppInfo, Int?) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    iconSize: Dp = 40.dp,
) {
    var levelMenu by remember(row.app.key, row.level) { mutableStateOf(false) }
    var limitMenu by remember(row.app.key, row.dailyLimitMin) { mutableStateOf(false) }
    val accent = row.level.accent()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app = row.app, loadIcon = loadIcon, size = iconSize)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = row.app.label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (row.isProtected) {
                            stringResource(R.string.protected_app)
                        } else {
                            levelLabel(row.level)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (row.isProtected) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.essential_app_short),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    MiniPill(
                        text = row.dailyLimitMin?.let {
                            stringResource(R.string.limit_minutes_short, it)
                        } ?: stringResource(R.string.limit_none_short),
                        accent = if (row.dailyLimitMin != null) Accent.CALENDAR else null,
                        enabled = !row.isProtected,
                        onClick = { limitMenu = true },
                        icon = Icons.Rounded.Timer,
                    )
                    DropdownMenu(expanded = limitMenu, onDismissRequest = { limitMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.limit_none)) },
                            onClick = {
                                limitMenu = false
                                onSetLimit(row.app, null)
                            },
                        )
                        for (m in LIMIT_PRESETS) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.limit_minutes, m)) },
                                onClick = {
                                    limitMenu = false
                                    onSetLimit(row.app, m)
                                },
                            )
                        }
                    }
                }
                Box {
                    MiniPill(
                        text = levelShort(row.level),
                        accent = accent,
                        enabled = !row.isProtected,
                        onClick = { levelMenu = true },
                        icon = Icons.Rounded.Visibility,
                    )
                    DropdownMenu(expanded = levelMenu, onDismissRequest = { levelMenu = false }) {
                        for (level in AppLevel.entries) {
                            DropdownMenuItem(
                                text = { Text(levelLabel(level)) },
                                onClick = {
                                    levelMenu = false
                                    onSetLevel(row.app, level)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPill(
    text: String,
    onClick: () -> Unit,
    accent: Accent?,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val colors = accent?.colors()
    Surface(
        shape = CircleShape,
        color = colors?.container?.copy(alpha = 0.55f)
            ?: MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        contentColor = colors?.content ?: MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
        ),
        modifier = Modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private val LIMIT_PRESETS = listOf(15, 30, 60, 120)

private fun AppLevel.accent(): Accent = when (this) {
    AppLevel.VISIBLE -> Accent.FOCUS
    AppLevel.TUCKED -> Accent.NOTES
    AppLevel.HIDDEN -> Accent.MOTIVATION
    AppLevel.PAUSE_GATED -> Accent.TODOS
}

@Composable
private fun levelLabel(level: AppLevel): String = stringResource(
    when (level) {
        AppLevel.VISIBLE -> R.string.level_visible
        AppLevel.TUCKED -> R.string.level_tucked
        AppLevel.HIDDEN -> R.string.level_hidden
        AppLevel.PAUSE_GATED -> R.string.level_gated
    },
)

@Composable
private fun levelShort(level: AppLevel): String = stringResource(
    when (level) {
        AppLevel.VISIBLE -> R.string.level_visible_short
        AppLevel.TUCKED -> R.string.level_tucked_short
        AppLevel.HIDDEN -> R.string.level_hidden_short
        AppLevel.PAUSE_GATED -> R.string.level_gated_short
    },
)
