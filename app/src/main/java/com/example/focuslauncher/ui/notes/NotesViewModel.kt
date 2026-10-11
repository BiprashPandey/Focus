package com.example.focuslauncher.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.notes.Note
import com.example.focuslauncher.data.notes.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    val uiState: StateFlow<NotesUiState> = repository.notes
        .map { NotesUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    /** Quick-capture from the home card. Blank input is ignored. */
    fun quickAdd(body: String) {
        viewModelScope.launch {
            try {
                repository.save(body)
            } catch (_: IllegalArgumentException) {
                // Ignore.
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { repository.setPinned(note.id, !note.pinned, note.body) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
