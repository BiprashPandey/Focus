package com.example.focuslauncher.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.calendar.CalEvent
import com.example.focuslauncher.data.calendar.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val events: List<CalEvent> = emptyList(),
    val hasPermission: Boolean = false,
    val loaded: Boolean = false,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val granted = repository.hasPermission()
            _state.value = CalendarUiState(
                events = if (granted) repository.nextEvents() else emptyList(),
                hasPermission = granted,
                loaded = true,
            )
        }
    }
}
