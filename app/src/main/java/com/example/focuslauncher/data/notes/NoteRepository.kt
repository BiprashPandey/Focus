package com.example.focuslauncher.data.notes

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
) {
    val notes: Flow<List<Note>> = dao.observeAll()

    suspend fun save(body: String, id: Long = 0, pinned: Boolean = false) {
        val clean = body.trim()
        require(clean.isNotEmpty())
        dao.upsert(Note(id = id, body = clean, pinned = pinned))
    }

    suspend fun setPinned(id: Long, pinned: Boolean, body: String) {
        dao.upsert(Note(id = id, body = body, pinned = pinned))
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
