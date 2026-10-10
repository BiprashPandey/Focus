package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
) : ViewModel() {

    val uiState: StateFlow<AppsUiState> = repository.apps
        .map { AppsUiState(apps = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState(isLoading = true))

    suspend fun loadIcon(app: AppInfo): Drawable? = repository.loadIcon(app)
}
