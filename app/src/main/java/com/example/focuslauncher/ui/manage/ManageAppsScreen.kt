package com.example.focuslauncher.ui.manage

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.drawer.AppIcon

@Composable
fun ManageAppsScreen(
    state: ManageAppsUiState,
    onQueryChange: (String) -> Unit,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    onSetLimit: (AppInfo, Int?) -> Unit = { _, _ -> },
    onClose: () -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.manage_apps_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.manage_apps_close))
                }
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.manage_apps_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.rows, key = { it.app.key }) { row ->
                    ManageRow(
                        row = row,
                        onSetLevel = onSetLevel,
                        onSetLimit = onSetLimit,
                        loadIcon = loadIcon,
                    )
                    HorizontalDivider()
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
) {
    var levelMenu by remember(row.app.key, row.level) { mutableStateOf(false) }
    var limitMenu by remember(row.app.key, row.dailyLimitMin) { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app = row.app, loadIcon = loadIcon)
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
        OutlinedButton(
            onClick = { limitMenu = true },
            enabled = !row.isProtected,
        ) {
            Text(text = row.dailyLimitMin?.let { stringResource(R.string.limit_minutes_short, it) }
                ?: stringResource(R.string.limit_none_short))
        }
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
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = { levelMenu = true },
            enabled = !row.isProtected,
        ) {
            Text(text = levelShort(row.level))
        }
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

private val LIMIT_PRESETS = listOf(15, 30, 60, 120)

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
