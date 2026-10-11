package com.example.focuslauncher.ui.motivation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.motivation.MotivationRepository
import com.example.focuslauncher.data.motivation.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MotivationUiState(
    val intention: String = "",
    val hasIntention: Boolean = false,
    val intentionDone: Boolean = false,
    val quote: Quote? = null,
    val tone: String = "gentle",
)

@HiltViewModel
class MotivationViewModel @Inject constructor(
    private val repository: MotivationRepository,
) : ViewModel() {

    private data class Loaded(
        val text: String = "",
        val done: Boolean = false,
        val has: Boolean = false,
        val quote: Quote? = null,
    )

    private val loaded = MutableStateFlow(Loaded())

    init {
        reload()
    }

    val uiState: StateFlow<MotivationUiState> =
        combine(loaded, repository.tone) { data, tone ->
            MotivationUiState(
                intention = data.text,
                hasIntention = data.has,
                intentionDone = data.done,
                quote = data.quote,
                tone = tone,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MotivationUiState())

    fun reload() {
        viewModelScope.launch {
            val key = repository.todayKey()
            val intention = repository.intention(key)
            val message = repository.dailyMessage()
            loaded.value = Loaded(
                text = intention?.text ?: "",
                done = intention?.completed == true,
                has = intention != null,
                quote = message.quote,
            )
        }
    }

    fun saveIntention(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.saveIntention(repository.todayKey(), clean)
            reload()
        }
    }

    fun toggleIntentionDone() {
        viewModelScope.launch {
            val key = repository.todayKey()
            val current = repository.intention(key) ?: return@launch
            repository.setIntentionDone(key, !current.completed)
            reload()
        }
    }

    fun setTone(tone: String) {
        viewModelScope.launch {
            try {
                repository.setTone(tone)
                reload()
            } catch (_: IllegalArgumentException) {
                // Unknown tone: ignore.
            }
        }
    }
}
