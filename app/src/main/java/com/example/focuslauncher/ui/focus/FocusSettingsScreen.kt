package com.example.focuslauncher.ui.focus

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.focuslauncher.domain.focus.FocusSchedule
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.ui.drawer.AppIcon
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FocusSettingsScreen(
    state: FocusSettingsUiState,
    onQueryChange: (String) -> Unit,
    onSelectProfile: (Long) -> Unit,
    onToggleAllowed: (AppInfo, Boolean) -> Unit,
    onDndToggle: (Boolean) -> Unit,
    onOpenDndSettings: () -> Unit,
    onSaveSchedule: (FocusSchedule) -> Unit,
    onClearSchedule: () -> Unit,
    onClose: () -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
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
                        text = stringResource(R.string.focus_settings_title),
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
            item(key = "profiles") {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.profiles.forEach { p ->
                        FilterChip(
                            selected = p.id == state.selectedId,
                            onClick = { onSelectProfile(p.id) },
                            label = { Text(p.name) },
                        )
                    }
                }
            }
            item(key = "dnd") {
                DndRow(
                    enabled = state.dndEnabled,
                    onToggle = onDndToggle,
                    onOpenSettings = onOpenDndSettings,
                )
            }
            item(key = "schedule") {
                ScheduleEditor(
                    current = state.schedule,
                    onSave = onSaveSchedule,
                    onClear = onClearSchedule,
                )
            }
            item(key = "allowed-header") {
                Text(
                    text = stringResource(R.string.focus_allowed_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item(key = "search") {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    label = { Text(stringResource(R.string.manage_apps_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(state.rows, key = { it.app.key }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = row.allowed,
                        enabled = !row.isProtected,
                        onCheckedChange = { onToggleAllowed(row.app, it) },
                    )
                    AppIcon(app = row.app, loadIcon = loadIcon)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = row.app.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DndRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.focus_dnd_title),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.focus_dnd_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        if (enabled) {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.focus_dnd_open_settings))
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun ScheduleEditor(
    current: FocusSchedule?,
    onSave: (FocusSchedule) -> Unit,
    onClear: () -> Unit,
) {
    var days by remember(current) { mutableStateOf(current?.days ?: emptySet()) }
    var start by remember(current) {
        mutableStateOf(current?.let { FocusSchedule.formatTime(it.startMin) } ?: "")
    }
    var end by remember(current) {
        mutableStateOf(current?.let { FocusSchedule.formatTime(it.endMin) } ?: "")
    }
    val startMin = FocusSchedule.parseTime(start)
    val endMin = FocusSchedule.parseTime(end)
    val valid = days.isNotEmpty() && startMin != null && endMin != null && startMin != endMin
    val locale = Locale.getDefault()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.focus_schedule_title),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (current != null) {
            Text(
                text = stringResource(R.string.focus_schedule_current, describe(current, locale)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day.value in days,
                    onClick = {
                        days = if (day.value in days) days - day.value else days + day.value
                    },
                    label = {
                        Text(day.getDisplayName(TextStyle.SHORT, locale))
                    },
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = start,
                onValueChange = { start = it.take(5) },
                label = { Text(stringResource(R.string.focus_schedule_start)) },
                placeholder = { Text("09:00") },
                singleLine = true,
                isError = start.isNotEmpty() && startMin == null,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = end,
                onValueChange = { end = it.take(5) },
                label = { Text(stringResource(R.string.focus_schedule_end)) },
                placeholder = { Text("12:00") },
                singleLine = true,
                isError = end.isNotEmpty() && endMin == null,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onSave(FocusSchedule(days, startMin!!, endMin!!)) },
                enabled = valid,
            ) {
                Text(stringResource(R.string.focus_schedule_save))
            }
            if (current != null) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.focus_schedule_clear))
                }
            }
        }
    }
    HorizontalDivider()
}

private fun describe(schedule: FocusSchedule, locale: Locale): String {
    val days = schedule.days.sorted()
        .joinToString(" ") { DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, locale) }
    return "$days ${FocusSchedule.formatTime(schedule.startMin)}–${FocusSchedule.formatTime(schedule.endMin)}"
}
