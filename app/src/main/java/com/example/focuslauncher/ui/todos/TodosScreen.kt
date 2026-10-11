package com.example.focuslauncher.ui.todos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.todos.Todo

@Composable
fun TodosScreen(
    state: TodosUiState,
    onToggle: (Todo) -> Unit,
    onDelete: (Long) -> Unit,
    onClearCompleted: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.todos_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.manage_apps_close),
                    )
                }
            }
            val open = state.all.filter { it.doneAt == null }
            val done = state.all.filter { it.doneAt != null }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(open, key = { it.id }) { todo ->
                    SwipeableTodoRow(todo = todo, onToggle = onToggle)
                    HorizontalDivider()
                }
                if (done.isNotEmpty()) {
                    item(key = "done-header") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SwipeableTodoRow(
                                todo = todo,
                                onToggle = onToggle,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { onDelete(todo.id) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.todos_delete),
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
            Spacer(modifier = Modifier.width(0.dp))
        }
    }
}
