package com.example.focuslauncher.ui.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.countdown
import com.example.focuslauncher.domain.focus.remaining
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.components.HomeCard
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.TimerFontFamily
import com.example.focuslauncher.ui.theme.colors

const val FOCUS_EXIT_WAIT_SECONDS = 10

@Composable
fun FocusCard(
    state: FocusUiState,
    onStart: (profileId: Long, durationMin: Int) -> Unit,
    onStop: (reason: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.FOCUS
    val active = state.active
    val profileName = active?.let { running ->
        state.profiles.firstOrNull { it.id == running.profileId }?.name
    }
    HomeCard(
        title = profileName ?: stringResource(R.string.focus_title),
        accent = accent,
        icon = Icons.Rounded.Timer,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        if (active == null) {
            IdleFocusContent(
                state = state,
                onStart = onStart,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FocusRing(
                    active = active,
                    now = state.now,
                    accent = accent,
                )
                Spacer(modifier = Modifier.size(12.dp))
                EndFocusControl(active = active, onStop = onStop, showHint = true)
            }
        }
    }
}

@Composable
private fun IdleFocusContent(
    state: FocusUiState,
    onStart: (Long, Int) -> Unit,
) {
    var selectedProfile by remember(state.profiles) {
        mutableIntStateOf(0)
    }
    var selectedDuration by remember { mutableIntStateOf(FocusDurations.POMODORO) }
    var customText by remember { mutableStateOf("") }
    val customMin = customText.toIntOrNull()?.takeIf { it in 1..480 }
    val profile = state.profiles.getOrNull(selectedProfile)
    val accent = Accent.FOCUS
    val colors = accent.colors()

    Text(
        text = stringResource(R.string.focus_profile_title),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.size(6.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        state.profiles.forEachIndexed { index, p ->
            FilterChip(
                selected = index == selectedProfile,
                onClick = { selectedProfile = index },
                shape = CircleShape,
                label = { Text(p.name, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = colors.container,
                    selectedLabelColor = colors.content,
                ),
            )
        }
    }
    Spacer(modifier = Modifier.size(12.dp))
    Text(
        text = stringResource(R.string.focus_length_title),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.size(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FocusDurations.presets.forEach { min ->
            DurationTile(
                minutes = min,
                selected = customMin == null && selectedDuration == min,
                onClick = {
                    selectedDuration = min
                    customText = ""
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(modifier = Modifier.size(8.dp))
    CustomMinutesField(
        value = customText,
        onValueChange = { customText = it.filter(Char::isDigit).take(3) },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.size(12.dp))
    GradientButton(
        text = stringResource(R.string.focus_start),
        onClick = { profile?.let { onStart(it.id, customMin ?: selectedDuration) } },
        enabled = profile != null,
        accent = accent,
        icon = Icons.Rounded.PlayArrow,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DurationTile(
    minutes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.FOCUS
    val colors = accent.colors()
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            colors.container
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                colors.bright.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = minutes.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = if (selected) colors.content else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.minutes_suffix),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    colors.content.copy(alpha = 0.72f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun CustomMinutesField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerHigh.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.30f)),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.custom_minutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    }
                },
            )
            Text(
                text = stringResource(R.string.minutes_suffix),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FocusRing(
    active: com.example.focuslauncher.domain.focus.FocusState.Active,
    now: Long,
    accent: Accent,
) {
    val colors = accent.colors()
    val total = (active.endsAt - active.startedAt).coerceAtLeast(1L)
    val fraction = (active.remaining(now).toFloat() / total).coerceIn(0f, 1f)
    Box(
        modifier = Modifier.size(168.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(168.dp)) {
            val stroke = 10.dp.toPx()
            drawCircle(
                color = colors.container.copy(alpha = 0.85f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        colors.bright,
                        colors.bright.copy(alpha = 0.55f),
                        colors.bright,
                    ),
                ),
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = active.countdown(now),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = TimerFontFamily,
                    fontWeight = FontWeight.Light,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.focus_in_progress),
                style = MaterialTheme.typography.labelSmall,
                color = colors.content,
            )
        }
    }
}

/**
 * Emergency exit shared by the full card and the pinned bar: tap End, then a
 * 10-second wait with an optional reason. Always dismissible.
 */
@Composable
fun EndFocusControl(
    active: com.example.focuslauncher.domain.focus.FocusState.Active,
    onStop: (String?) -> Unit,
    showHint: Boolean,
    modifier: Modifier = Modifier,
) {
    var confirming by remember(active.sessionId) { mutableStateOf(false) }
    var reason by remember(active.sessionId) { mutableStateOf("") }
    var waitLeft by remember(active.sessionId) { mutableIntStateOf(FOCUS_EXIT_WAIT_SECONDS) }
    LaunchedEffect(confirming, active.sessionId) {
        if (confirming) {
            waitLeft = FOCUS_EXIT_WAIT_SECONDS
            while (waitLeft > 0) {
                kotlinx.coroutines.delay(1_000)
                waitLeft--
            }
        }
    }
    val colors = Accent.FOCUS.colors()

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!confirming) {
            if (showHint) {
                Text(
                    text = stringResource(R.string.focus_end_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = { confirming = true },
                    ),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, colors.bright.copy(alpha = 0.5f)),
            ) {
                Text(
                    text = stringResource(R.string.focus_end),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        } else {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.focus_exit_reason)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onStop(reason.ifBlank { null }) },
                    enabled = waitLeft <= 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        if (waitLeft > 0) {
                            stringResource(R.string.focus_exit_wait, waitLeft)
                        } else {
                            stringResource(R.string.focus_exit_confirm)
                        },
                    )
                }
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.pause_close))
                }
            }
        }
    }
}
