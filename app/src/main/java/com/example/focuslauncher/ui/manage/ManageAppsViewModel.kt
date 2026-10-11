package com.example.focuslauncher.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.AppConfigRepository
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.domain.rules.AppListRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageAppRow(
    val app: AppInfo,
    val level: AppLevel,
    val dailyLimitMin: Int?,
    val isProtected: Boolean,
)

data class ManageAppsUiState(
    val rows: List<ManageAppRow> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
)

@HiltViewModel
class ManageAppsViewModel @Inject constructor(
    appsRepository: AppsRepository,
    private val appConfigRepository: AppConfigRepository,
    protectedApps: ProtectedApps,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val protectedPackages = MutableStateFlow(emptySet<String>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
        }
    }

    val uiState: StateFlow<ManageAppsUiState> =
        combine(
            appsRepository.apps,
            appConfigRepository.configs,
            protectedPackages,
            _query,
        ) { apps, configs, protected, query ->
            val rows = apps.map { app ->
                val cfg = configs[app.packageName]
                val level = if (app.packageName in protected) {
                    AppLevel.VISIBLE
                } else {
                    cfg?.level ?: AppLevel.VISIBLE
                }
                ManageAppRow(app, level, cfg?.dailyLimitMin, app.packageName in protected)
            }
            val filtered = AppListRules.filter(rows.map { it.app }, query)
            val byKey = rows.associateBy { it.app.key }
            ManageAppsUiState(
                rows = filtered.mapNotNull { byKey[it.key] },
                query = query,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageAppsUiState(isLoading = true))

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun setLevel(app: AppInfo, level: AppLevel) {
        if (app.packageName in protectedPackages.value) return
        viewModelScope.launch(Dispatchers.IO) {
            appConfigRepository.setLevel(app.packageName, level)
        }
    }

    fun setDailyLimit(app: AppInfo, minutes: Int?) {
        if (app.packageName in protectedPackages.value) return
        viewModelScope.launch(Dispatchers.IO) {
            appConfigRepository.setDailyLimit(app.packageName, minutes)
        }
    }
}
