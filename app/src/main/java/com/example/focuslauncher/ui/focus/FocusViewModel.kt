package com.example.focuslauncher.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.data.focus.FocusProfile
import com.example.focuslauncher.domain.focus.FocusState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusUiState(
    val active: FocusState.Active? = null,
    val profiles: List<FocusProfile> = emptyList(),
    /** Re-emitted every second while active so the timer recomposes. */
    val now: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val manager: FocusManager,
) : ViewModel() {

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    val uiState: StateFlow<FocusUiState> =
        combine(manager.state, manager.allProfiles, ticker) { state, profiles, now ->
            FocusUiState(
                active = state as? FocusState.Active,
                profiles = profiles,
                now = now,
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusUiState())

    fun start(profileId: Long, durationMin: Int) {
        viewModelScope.launch { manager.start(profileId, durationMin) }
    }

    fun stop(exitReason: String? = null) {
        viewModelScope.launch { manager.stop(exitReason) }
    }

    fun setAllowedApps(profileId: Long, packages: Set<String>) {
        viewModelScope.launch { manager.setAllowedApps(profileId, packages) }
    }

    fun setDnd(profileId: Long, enabled: Boolean) {
        viewModelScope.launch { manager.setDnd(profileId, enabled) }
    }

    fun setSchedule(profileId: Long, json: String?) {
        viewModelScope.launch { manager.setSchedule(profileId, json) }
    }
}
