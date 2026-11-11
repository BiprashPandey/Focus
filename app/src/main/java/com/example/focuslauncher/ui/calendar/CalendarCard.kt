package com.example.focuslauncher.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.calendar.CalEvent
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.components.HomeCard
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.AccentColors
import com.example.focuslauncher.ui.theme.colors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarCard(
    state: CalendarUiState,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.CALENDAR
    val colors = accent.colors()
    HomeCard(
        title = stringResource(R.string.calendar_title),
        accent = accent,
        icon = Icons.Rounded.CalendarMonth,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        when {
            !state.hasPermission -> {
                Text(
                    text = stringResource(R.string.calendar_connect_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(12.dp))
                GradientButton(
                    text = stringResource(R.string.calendar_connect),
                    onClick = onConnect,
                    accent = accent,
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.events.isEmpty() -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EventBusy,
                        contentDescription = null,
                        tint = colors.bright,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (state.loaded) R.string.calendar_empty else R.string.loading_apps,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.events.forEach { event ->
                        CalendarRow(event = event, accentColor = colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarRow(
    event: CalEvent,
    accentColor: AccentColors,
) {
    val locale = remember { Locale.getDefault() }
    val zone = remember { ZoneId.systemDefault() }
    val start = remember(event.start, zone) {
        Instant.ofEpochMilli(event.start).atZone(zone)
    }
    val today = LocalDate.now(zone)
    val dayLabel = when (start.toLocalDate()) {
        today -> stringResource(R.string.calendar_today)
        today.plusDays(1) -> stringResource(R.string.calendar_tomorrow)
        else -> start.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))
    }
    val timeLabel = start.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.container.copy(alpha = 0.65f),
            modifier = Modifier.width(70.dp),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.content.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor.content,
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = event.title.ifBlank { stringResource(R.string.calendar_untitled) },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}
