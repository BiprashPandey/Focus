package com.example.focuslauncher.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.automirrored.rounded.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.notes.Note
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.EmptyState
import com.example.focuslauncher.ui.components.HomeCard
import com.example.focuslauncher.ui.components.QuickInput
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

@Composable
fun NotesCard(
    state: NotesUiState,
    onQuickAdd: (String) -> Unit,
    onOpenNotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val accent = Accent.NOTES
    val colors = accent.colors()
    val latest = state.notes.firstOrNull()
    HomeCard(
        title = stringResource(R.string.notes_title),
        accent = accent,
        icon = Icons.AutoMirrored.Rounded.StickyNote2,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        QuickInput(
            value = draft,
            onValueChange = { draft = it },
            placeholder = stringResource(R.string.notes_quick_add),
            onSubmit = {
                onQuickAdd(draft)
                draft = ""
            },
            accent = accent,
        )
        if (latest != null) {
            Spacer(modifier = Modifier.size(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container.copy(alpha = 0.55f))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .size(width = 3.dp, height = 40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.bright),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = latest.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.content,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (latest.pinned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = stringResource(R.string.pinned_note),
                                tint = colors.content.copy(alpha = 0.75f),
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.pinned_note),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.content.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onOpenNotes) {
                    Text(stringResource(R.string.notes_open, state.notes.size))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.StickyNote2,
                    contentDescription = null,
                    tint = colors.bright,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.notes_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun NotesScreen(
    state: NotesUiState,
    onTogglePin: (Note) -> Unit,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.NOTES
    val colors = accent.colors()
    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.notes_title),
                onClose = onClose,
            )
            if (state.notes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Rounded.StickyNote2,
                        title = stringResource(R.string.notes_empty),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { onTogglePin(note) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = stringResource(R.string.notes_pin),
                                        tint = if (note.pinned) {
                                            colors.bright
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                                Text(
                                    text = note.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { onDelete(note.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = stringResource(R.string.todos_delete),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
