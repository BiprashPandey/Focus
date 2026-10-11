package com.example.focuslauncher.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.data.focus.FocusProfile
import com.example.focuslauncher.domain.focus.FocusFilter
import com.example.focuslauncher.domain.focus.FocusSchedule
import com.example.focuslauncher.domain.model.AppInfo
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

data class AllowedAppRow(
    val app: AppInfo,
    val allowed: Boolean,
    val isProtected: Boolean,
)

data class FocusSettingsUiState(
    val profiles: List<FocusProfile> = emptyList(),
    val selectedId: Long? = null,
    val rows: List<AllowedAppRow> = emptyList(),
    val query: String = "",
    val schedule: FocusSchedule? = null,
    val dndEnabled: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class FocusSettingsViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val manager: FocusManager,
    private val protectedApps: ProtectedApps,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val selectedId = MutableStateFlow<Long?>(null)
    private val protectedPackages = MutableStateFlow(emptySet<String>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
        }
    }

    val uiState: StateFlow<FocusSettingsUiState> =
        combine(
            appsRepository.apps,
            manager.allProfiles,
            selectedId,
            protectedPackages,
            _query,
        ) { apps, profiles, sel, protected, query ->
            val selected = profiles.firstOrNull { it.id == sel } ?: profiles.firstOrNull()
            val allowed = selected?.let { FocusFilter.parseAllowed(it.allowedAppsJson) }
                ?: emptySet()
            val filtered = AppListRules.filter(apps, query)
            FocusSettingsUiState(
                profiles = profiles,
                selectedId = selected?.id,
                rows = filtered.map { app ->
                    AllowedAppRow(
                        app = app,
                        allowed = app.packageName in allowed || app.packageName in protected,
                        isProtected = app.packageName in protected,
                    )
                },
                query = query,
                schedule = FocusSchedule.fromJson(selected?.scheduleJson),
                dndEnabled = selected?.dndEnabled == true,
                isLoading = false,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            FocusSettingsUiState(),
        )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onSelectProfile(id: Long) {
        selectedId.value = id
    }

    fun onToggleAllowed(app: AppInfo, checked: Boolean) {
        val id = uiState.value.selectedId ?: return
        if (app.packageName in protectedPackages.value) return
        viewModelScope.launch(Dispatchers.IO) {
            // Re-read from latest state instead of a stale capture.
            val latest = uiState.value.rows
                .filter { it.allowed && !it.isProtected }
                .map { it.app.packageName }.toMutableSet()
            if (checked) latest.add(app.packageName) else latest.remove(app.packageName)
            manager.setAllowedApps(id, latest)
        }
    }

    fun onDndToggle(enabled: Boolean) {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setDnd(id, enabled) }
    }

    fun onSaveSchedule(schedule: FocusSchedule) {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setSchedule(id, schedule.toJson()) }
    }

    fun onClearSchedule() {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setSchedule(id, null) }
    }
}
