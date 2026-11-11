package com.example.focuslauncher.ui.motivation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.settings.Tones
import com.example.focuslauncher.ui.components.HomeCard
import com.example.focuslauncher.ui.components.QuickInput
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

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
    val accent = Accent.MOTIVATION
    val colors = accent.colors()
    HomeCard(
        title = stringResource(R.string.intention_title),
        accent = accent,
        icon = Icons.Rounded.AutoAwesome,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        if (!state.hasIntention) {
            QuickInput(
                value = draft,
                onValueChange = { draft = it },
                placeholder = stringResource(R.string.intention_hint),
                onSubmit = { onSaveIntention(draft) },
                accent = accent,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.intentionDone) {
                                colors.bright
                            } else {
                                colors.container
                            },
                        )
                        .clickable(
                            role = Role.Checkbox,
                            onClick = onToggleIntentionDone,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.intentionDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.content,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = state.intention,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (state.intentionDone) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    },
                    color = if (state.intentionDone) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        state.quote?.let { quote ->
            Spacer(modifier = Modifier.size(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.container.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "“",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.bright.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.TopStart),
                )
                Column(modifier = Modifier.padding(start = 20.dp, top = 4.dp)) {
                    Text(
                        text = quote.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.content,
                    )
                    Text(
                        text = "— ${quote.by}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.content.copy(alpha = 0.72f),
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.tone_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Tones.all.forEach { tone ->
                    FilterChip(
                        selected = state.tone == tone,
                        onClick = { onToneChange(tone) },
                        shape = CircleShape,
                        label = {
                            Text(
                                text = toneLabel(tone),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = colors.container,
                            selectedLabelColor = colors.content,
                        ),
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
