package com.example.focuslauncher.ui.todos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.todos.Todo
import com.example.focuslauncher.ui.components.HomeCard
import com.example.focuslauncher.ui.components.QuickInput
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

@Composable
fun TodosCard(
    state: TodosUiState,
    onAdd: (String) -> Unit,
    onToggle: (Todo) -> Unit,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val accent = Accent.TODOS
    val colors = accent.colors()
    val done = state.all.count { it.doneAt != null }
    HomeCard(
        title = stringResource(R.string.todos_title),
        accent = accent,
        icon = Icons.Rounded.TaskAlt,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        trailing = {
            if (state.all.isNotEmpty()) {
                StatPill(
                    text = stringResource(R.string.done_count, done, state.all.size),
                    accent = accent,
                )
            }
        },
    ) {
        QuickInput(
            value = draft,
            onValueChange = { draft = it },
            placeholder = stringResource(R.string.todos_quick_add),
            onSubmit = {
                onAdd(draft)
                draft = ""
            },
            accent = accent,
        )
        if (state.top.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.bright,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.todos_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                state.top.forEach { todo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Checkbox) { onToggle(todo) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = todo.doneAt != null,
                            onCheckedChange = { onToggle(todo) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.bright,
                                checkmarkColor = colors.container,
                                uncheckedColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (todo.doneAt != null) {
                                TextDecoration.LineThrough
                            } else {
                                null
                            },
                            color = if (todo.doneAt != null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        if (state.all.size > 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onOpenList) {
                    Text(stringResource(R.string.todos_see_all, state.all.size))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTodoRow(
    todo: Todo,
    onToggle: (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onToggle(todo)
            }
            false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = todo.doneAt != null,
                onCheckedChange = { onToggle(todo) },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (todo.doneAt != null) {
                    TextDecoration.LineThrough
                } else {
                    null
                },
                color = if (todo.doneAt != null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            )
        }
    }
}
