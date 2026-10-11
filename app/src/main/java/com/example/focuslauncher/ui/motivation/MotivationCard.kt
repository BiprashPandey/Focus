package com.example.focuslauncher.ui.motivation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.settings.Tones

@Composable
fun MotivationCard(
    state: MotivationUiState,
    onSaveIntention: (String) -> Unit,
    onToggleIntentionDone: () -> Unit,
    onToneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(state.intention, state.hasIntention) {
        mutableStateOf(if (state.hasIntention) "" else state.intention)
    }
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.intention_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!state.hasIntention) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        label = { Text(stringResource(R.string.intention_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSaveIntention(draft) },
                        enabled = draft.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.intention_set))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.intentionDone,
                        onCheckedChange = { onToggleIntentionDone() },
                    )
                    Text(
                        text = state.intention,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            state.quote?.let { quote ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "“${quote.text}”",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "— ${quote.by}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tones.all.forEach { tone ->
                    FilterChip(
                        selected = state.tone == tone,
                        onClick = { onToneChange(tone) },
                        label = { Text(toneLabel(tone)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun toneLabel(tone: String): String = stringResource(
    when (tone) {
        Tones.COACH -> R.string.tone_coach
        Tones.STOIC -> R.string.tone_stoic
        Tones.FUNNY -> R.string.tone_funny
        else -> R.string.tone_gentle
    },
)
