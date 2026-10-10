package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.rules.AppListRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<AppsUiState> = combine(repository.apps, _query) { apps, query ->
        AppsUiState(
            apps = AppListRules.filter(apps, query),
            isLoading = false,
            query = query,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState(isLoading = true))

    fun onQueryChange(query: String) {
        _query.value = query
    }

    suspend fun loadIcon(app: AppInfo): Drawable? = repository.loadIcon(app)

    fun launch(app: AppInfo): Boolean = repository.launch(app)
}
