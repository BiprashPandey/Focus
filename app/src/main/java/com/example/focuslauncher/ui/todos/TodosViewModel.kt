package com.example.focuslauncher.ui.todos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.todos.Todo
import com.example.focuslauncher.data.todos.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodosUiState(
    val top: List<Todo> = emptyList(),
    val all: List<Todo> = emptyList(),
)

@HiltViewModel
class TodosViewModel @Inject constructor(
    private val repository: TodoRepository,
) : ViewModel() {

    val uiState: StateFlow<TodosUiState> =
        combine(repository.openTodos, repository.allTodos) { open, all ->
            TodosUiState(top = open.take(3), all = all)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodosUiState())

    fun add(title: String) {
        viewModelScope.launch {
            try {
                repository.add(title)
            } catch (_: IllegalArgumentException) {
                // Blank input: ignore.
            }
        }
    }

    fun toggle(todo: Todo) {
        viewModelScope.launch { repository.setDone(todo.id, todo.doneAt == null) }
    }

    fun clearCompleted() {
        viewModelScope.launch { repository.clearCompleted() }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
