package com.example.focuslauncher.data.todos

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val dao: TodoDao,
) {
    val openTodos: Flow<List<Todo>> = dao.observeOpen()
    val allTodos: Flow<List<Todo>> = dao.observeAll()

    suspend fun add(title: String, priority: Int = 1, dueAt: Long? = null) {
        val clean = title.trim()
        require(clean.isNotEmpty())
        dao.insert(Todo(title = clean, priority = priority.coerceIn(0, 2), dueAt = dueAt))
    }

    suspend fun setDone(id: Long, done: Boolean) {
        dao.setDone(id, if (done) System.currentTimeMillis() else null)
    }

    suspend fun clearCompleted() = dao.clearCompleted()

    suspend fun delete(id: Long) = dao.delete(id)
}
