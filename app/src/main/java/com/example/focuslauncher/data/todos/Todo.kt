package com.example.focuslauncher.data.todos

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String? = null,
    val dueAt: Long? = null,
    /** 0 = low, 1 = medium, 2 = high. */
    val priority: Int = 1,
    /** Null while open; epoch millis when completed. */
    val doneAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE doneAt IS NULL ORDER BY priority DESC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, createdAt ASC")
    fun observeOpen(): Flow<List<Todo>>

    @Query("SELECT * FROM todos ORDER BY CASE WHEN doneAt IS NULL THEN 0 ELSE 1 END, priority DESC, createdAt ASC")
    fun observeAll(): Flow<List<Todo>>

    @Insert
    suspend fun insert(todo: Todo): Long

    @Query("UPDATE todos SET doneAt = :doneAt WHERE id = :id")
    suspend fun setDone(id: Long, doneAt: Long?)

    @Query("DELETE FROM todos WHERE doneAt IS NOT NULL")
    suspend fun clearCompleted()

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: Long)
}
