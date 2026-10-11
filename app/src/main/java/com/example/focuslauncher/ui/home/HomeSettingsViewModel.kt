package com.example.focuslauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.settings.HomeLayout
import com.example.focuslauncher.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val layout: StateFlow<HomeLayout> = settings.homeLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeLayout())

    fun move(card: String, up: Boolean) {
        val order = layout.value.order.toMutableList()
        val i = order.indexOf(card)
        val j = if (up) i - 1 else i + 1
        if (i < 0 || j !in order.indices) return
        order[i] = order[j].also { order[j] = order[i] }
        viewModelScope.launch { settings.setCardOrder(order) }
    }

    fun setHidden(card: String, hidden: Boolean) {
        viewModelScope.launch { settings.setCardHidden(card, hidden) }
    }

    fun setLayout(layout: String) {
        viewModelScope.launch {
            try {
                settings.setLayout(layout)
            } catch (_: IllegalArgumentException) {
                // Unknown layout: ignore.
            }
        }
    }
}
