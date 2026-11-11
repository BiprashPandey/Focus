package com.example.focuslauncher.ui.focus

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.FocusSchedule
import com.example.focuslauncher.domain.focus.countdown
import com.example.focuslauncher.domain.focus.remaining
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.EmptyState
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.SearchField
import com.example.focuslauncher.ui.components.SettingsGroup
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.drawer.AppIcon
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.TimerFontFamily
import com.example.focuslauncher.ui.theme.colors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FocusSettingsScreen(
    state: FocusSettingsUiState,
    focusState: FocusUiState,
    onStartFocus: (Long, Int) -> Unit,
    onStopFocus: (String?) -> Unit,
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
    iconSize: Dp = 40.dp,
) {
    BackHandler { onClose() }
    val accent = Accent.FOCUS
    val colors = accent.colors()

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = stringResource(R.string.focus_settings_title),
                    subtitle = stringResource(R.string.focus_settings_subtitle),
                    onClose = onClose,
                )
            }
            item(key = "hero") {
                FocusHero(
                    focusState = focusState,
                    selectedId = state.selectedId,
                    onStartFocus = onStartFocus,
                    onStopFocus = onStopFocus,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "profiles") {
                SettingsGroup(title = stringResource(R.string.focus_profile_title)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.profiles.forEach { p ->
                            FilterChip(
                                shape = CircleShape,
                                selected = p.id == state.selectedId,
                                onClick = { onSelectProfile(p.id) },
                                label = { Text(p.name) },
                            )
                        }
                    }
                }
            }
            item(key = "dnd") {
                SettingsGroup(title = null) {
                    DndRow(
                        enabled = state.dndEnabled,
                        hasAccess = state.dndAccessGranted,
                        onToggle = onDndToggle,
                        onOpenSettings = onOpenDndSettings,
                    )
                }
            }
            item(key = "schedule") {
                SettingsGroup(title = stringResource(R.string.focus_schedule_title)) {
                    ScheduleEditor(
                        current = state.schedule,
                        onSave = onSaveSchedule,
                        onClear = onClearSchedule,
                    )
                }
            }
            item(key = "allowed-header") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.focus_allowed_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        text = stringResource(
                            R.string.apps_count,
                            state.rows.count { it.allowed },
                        ),
                        accent = accent,
                    )
                }
            }
            item(key = "search") {
                SearchField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = stringResource(R.string.manage_apps_search),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            if (state.rows.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.no_apps_found),
                    )
                }
            } else {
                items(state.rows, key = { it.app.key }) { row ->
                    AllowedRow(
                        row = row,
                        onToggle = onToggleAllowed,
                        loadIcon = loadIcon,
                        iconSize = iconSize,
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusHero(
    focusState: FocusUiState,
    selectedId: Long?,
    onStartFocus: (Long, Int) -> Unit,
    onStopFocus: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.FOCUS
    val colors = accent.colors()
    val active = focusState.active
    var duration by rememberSaveable { mutableIntStateOf(FocusDurations.POMODORO) }

    GlassCard(
        accent = accent,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
    ) {
        if (active != null) {
            val profile = focusState.profiles.firstOrNull { it.id == active.profileId }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                    val total = (active.endsAt - active.startedAt).coerceAtLeast(1L)
                    CircularProgressIndicator(
                        progress = {
                            (active.remaining(focusState.now).toFloat() / total).coerceIn(0f, 1f)
                        },
                        modifier = Modifier.size(92.dp),
                        color = colors.bright,
                        trackColor = colors.container.copy(alpha = 0.5f),
                        strokeWidth = 5.dp,
                    )
                    Text(
                        text = active.countdown(focusState.now),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = TimerFontFamily,
                        color = colors.content,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.focus_in_progress),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.content,
                    )
                    Text(
                        text = profile?.name ?: stringResource(R.string.focus_session),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            GradientButton(
                text = stringResource(R.string.focus_end),
                onClick = { onStopFocus(null) },
                accent = accent,
                icon = Icons.Rounded.StopCircle,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CenterFocusStrong,
                        contentDescription = null,
                        tint = colors.bright,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.focus_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.focus_settings_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(R.string.focus_length_title),
                style = MaterialTheme.typography.labelLarge,
                color = colors.content,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FocusDurations.presets.forEach { minutes ->
                    FilterChip(
                        shape = CircleShape,
                        selected = duration == minutes,
                        onClick = { duration = minutes },
                        label = { Text(stringResource(R.string.focus_minutes_short, minutes)) },
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            GradientButton(
                text = stringResource(R.string.focus_start_now),
                onClick = { selectedId?.let { onStartFocus(it, duration) } },
                enabled = selectedId != null,
                accent = accent,
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AllowedRow(
    row: AllowedAppRow,
    onToggle: (AppInfo, Boolean) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    iconSize: Dp,
) {
    val accent = Accent.FOCUS
    val colors = accent.colors()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = row.allowed,
                enabled = !row.isProtected,
                onCheckedChange = { onToggle(row.app, it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.bright,
                    checkmarkColor = colors.container,
                ),
            )
            AppIcon(app = row.app, loadIcon = loadIcon, size = iconSize)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = row.app.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (row.isProtected) {
                StatPill(
                    text = stringResource(R.string.essential_app_short),
                    accent = Accent.MOTIVATION,
                )
            }
        }
    }
}

@Composable
private fun DndRow(
    enabled: Boolean,
    hasAccess: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
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
                if (!hasAccess) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.focus_dnd_missing_access),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { checked ->
                    if (hasAccess) onToggle(checked) else onOpenSettings()
                },
            )
        }
        if (!hasAccess) {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.focus_dnd_grant))
            }
        } else if (enabled) {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.focus_dnd_open_settings))
            }
        }
    }
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

    Column(modifier = Modifier.padding(16.dp)) {
        if (current != null) {
            Text(
                text = stringResource(R.string.focus_schedule_current, describe(current, locale)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    shape = CircleShape,
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
        Spacer(modifier = Modifier.size(8.dp))
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
        Spacer(modifier = Modifier.size(8.dp))
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
}

private fun describe(schedule: FocusSchedule, locale: Locale): String {
    val days = schedule.days.sorted()
        .joinToString(" ") { DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, locale) }
    return "$days ${FocusSchedule.formatTime(schedule.startMin)}–${FocusSchedule.formatTime(schedule.endMin)}"
}
