package com.example.focuslauncher.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.settings.SettingsRepository
import com.example.focuslauncher.data.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val config: StateFlow<ThemeConfig> = settings.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeConfig())

    fun setMode(mode: String) = launch { settings.setThemeMode(mode) }
    fun setSource(source: String) = launch { settings.setColorSource(source) }
    fun setSeed(argb: Long) = launch { settings.setSeedColor(argb) }
    fun setCorner(style: String) = launch { settings.setCornerStyle(style) }
    fun setIconSize(dp: Int) = launch { settings.setIconSize(dp) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (_: IllegalArgumentException) {
                // Invalid value: ignore.
            }
        }
    }
}
