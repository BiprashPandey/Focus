package com.example.focuslauncher.ui.todos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.todos.Todo
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.EmptyState
import com.example.focuslauncher.ui.components.QuickInput
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.theme.Accent

@Composable
fun TodosScreen(
    state: TodosUiState,
    onAdd: (String) -> Unit,
    onToggle: (Todo) -> Unit,
    onDelete: (Long) -> Unit,
    onClearCompleted: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onClose() }
    var draft by remember { mutableStateOf("") }
    val accent = Accent.TODOS
    val open = state.all.filter { it.doneAt == null }
    val done = state.all.filter { it.doneAt != null }

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.todos_title),
                subtitle = stringResource(R.string.todos_subtitle),
                onClose = onClose,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatPill(
                    text = stringResource(R.string.open_count, open.size),
                    accent = accent,
                )
                if (done.isNotEmpty()) {
                    StatPill(
                        text = stringResource(R.string.todos_done, done.size),
                        accent = null,
                    )
                }
            }
            QuickInput(
                value = draft,
                onValueChange = { draft = it },
                placeholder = stringResource(R.string.todos_quick_add),
                onSubmit = {
                    onAdd(draft)
                    draft = ""
                },
                accent = accent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (open.isEmpty() && done.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Rounded.CheckCircle,
                        title = stringResource(R.string.todos_empty),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(open, key = { it.id }) { todo ->
                        TodoCardRow(todo = todo, onToggle = onToggle)
                    }
                    if (done.isNotEmpty()) {
                        item(key = "done-header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 10.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.todos_done, done.size),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = onClearCompleted) {
                                    Text(stringResource(R.string.todos_clear))
                                }
                            }
                        }
                        items(done, key = { it.id }) { todo ->
                            TodoCardRow(
                                todo = todo,
                                onToggle = onToggle,
                                onDelete = { onDelete(todo.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoCardRow(
    todo: Todo,
    onToggle: (Todo) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwipeableTodoRow(
                todo = todo,
                onToggle = onToggle,
                modifier = Modifier.weight(1f),
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.todos_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
