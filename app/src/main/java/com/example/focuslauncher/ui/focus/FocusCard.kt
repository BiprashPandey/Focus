package com.example.focuslauncher.ui.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.focus.FocusDurations
import com.example.focuslauncher.domain.focus.countdown

const val FOCUS_EXIT_WAIT_SECONDS = 10

@Composable
fun FocusCard(
    state: FocusUiState,
    onStart: (profileId: Long, durationMin: Int) -> Unit,
    onStop: (reason: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        val active = state.active
        if (active == null) {
            IdleFocusContent(
                state = state,
                onStart = onStart,
            )
        } else {
            ActiveFocusContent(
                state = state,
                onStop = onStop,
            )
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.focus_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.profiles.forEachIndexed { index, p ->
                FilterChip(
                    shape = CircleShape,
                    selected = index == selectedProfile,
                    onClick = { selectedProfile = index },
                    label = { Text(p.name) },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusDurations.presets.forEach { min ->
                FilterChip(
                    shape = CircleShape,
                    selected = customMin == null && selectedDuration == min,
                    onClick = {
                        selectedDuration = min
                        customText = ""
                    },
                    label = { Text(stringResource(R.string.focus_minutes_short, min)) },
                )
            }
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it.filter(Char::isDigit).take(3) },
                label = { Text(stringResource(R.string.focus_custom)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(110.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { profile?.let { onStart(it.id, customMin ?: selectedDuration) } },
            enabled = profile != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.focus_start))
        }
    }
}

@Composable
private fun ActiveFocusContent(
    state: FocusUiState,
    onStop: (String?) -> Unit,
) {
    val active = state.active ?: return
    val profileName = state.profiles.firstOrNull { it.id == active.profileId }?.name
        ?: stringResource(R.string.focus_session)

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = profileName, style = MaterialTheme.typography.titleMedium)
        Text(
            text = active.countdown(state.now),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        EndFocusControl(active = active, onStop = onStop, showHint = true)
    }
}

/**
 * Emergency exit shared by the full card and the pinned bar: long-press End,
 * then a 10-second wait with an optional reason. Always dismissible.
 */
@OptIn(ExperimentalFoundationApi::class)
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

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!confirming) {
            if (showHint) {
                Text(
                    text = stringResource(R.string.focus_end_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { confirming = true },
                    ),
            ) {
                Text(stringResource(R.string.focus_end))
            }
        } else {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(stringResource(R.string.focus_exit_reason)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
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
