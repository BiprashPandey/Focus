package com.example.focuslauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslauncher.R
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeHeader(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
    onManageClick: () -> Unit,
    onUsageClick: () -> Unit,
    onFocusClick: () -> Unit,
    onHomeClick: () -> Unit,
    onThemeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 18.dp),
    ) {
        ClockBlock()
        Spacer(modifier = Modifier.size(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            QuickAction(
                label = stringResource(R.string.focus_button),
                icon = Icons.Rounded.Timer,
                accent = Accent.FOCUS,
                onClick = onFocusClick,
                modifier = Modifier.weight(1f),
            )
            QuickAction(
                label = stringResource(R.string.quick_apps),
                icon = Icons.Rounded.GridView,
                accent = Accent.NOTES,
                onClick = onManageClick,
                modifier = Modifier.weight(1f),
            )
            QuickAction(
                label = stringResource(R.string.quick_limits),
                icon = Icons.Rounded.BarChart,
                accent = Accent.TODOS,
                onClick = onUsageClick,
                modifier = Modifier.weight(1f),
            )
            QuickAction(
                label = stringResource(R.string.quick_home),
                icon = Icons.Rounded.DashboardCustomize,
                accent = Accent.CALENDAR,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f),
            )
            QuickAction(
                label = stringResource(R.string.quick_theme),
                icon = Icons.Rounded.Palette,
                accent = Accent.MOTIVATION,
                onClick = onThemeClick,
                modifier = Modifier.weight(1f),
            )
        }
        if (!isDefaultLauncher) {
            Spacer(modifier = Modifier.size(18.dp))
            if (isRoleAvailable) {
                SetupBanner(onRequestDefault = onRequestDefault)
            } else {
                Text(
                    text = stringResource(R.string.default_launcher_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClockBlock() {
    val context = LocalContext.current
    val locale = remember { Locale.getDefault() }
    val zone = remember { ZoneId.systemDefault() }
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    var now by remember { mutableStateOf(LocalDateTime.now(zone)) }
    LaunchedEffect(zone) {
        while (true) {
            val current = LocalDateTime.now(zone)
            val millisIntoMinute = current.second * 1_000L + current.nano / 1_000_000
            delay(60_000L - millisIntoMinute)
            now = LocalDateTime.now(zone)
        }
    }
    val timeFormatter = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm", locale)
    }
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
    }
    val greeting = when (now.hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..16 -> R.string.greeting_afternoon
        in 17..21 -> R.string.greeting_evening
        else -> R.string.greeting_night
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(greeting),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            FocusMark(size = 20.dp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = now.format(timeFormatter),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = now.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    accent: Accent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = accent.colors()
    Column(
        modifier = modifier
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(colors.container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SetupBanner(onRequestDefault: () -> Unit) {
    val accent = Accent.MOTIVATION
    GlassCard(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.colors().container, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = accent.colors().content,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_setup_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.home_setup_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        GradientButton(
            text = stringResource(R.string.set_as_default),
            onClick = onRequestDefault,
            accent = accent,
            icon = Icons.Rounded.Home,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Small focus-ring mark used as the launcher brand. */
@Composable
fun FocusMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = size.toPx() * 0.12f
        drawCircle(
            color = color,
            radius = size.toPx() * 0.34f,
            center = center,
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = color,
            radius = size.toPx() * 0.09f,
            center = center,
        )
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = size.toPx() * 0.48f,
            center = Offset(center.x, center.y),
            style = Stroke(width = stroke * 0.5f),
        )
    }
}
