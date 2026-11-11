package com.example.focuslauncher.ui.focus

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.apps.MusicApps
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.data.focus.FocusProfile
import com.example.focuslauncher.domain.focus.FocusFilter
import com.example.focuslauncher.domain.focus.FocusSchedule
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.rules.AppListRules
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    /** Music players stay available automatically, without a per-profile toggle. */
    val isMusic: Boolean = false,
)

data class FocusSettingsUiState(
    val profiles: List<FocusProfile> = emptyList(),
    val selectedId: Long? = null,
    val rows: List<AllowedAppRow> = emptyList(),
    val query: String = "",
    val schedule: FocusSchedule? = null,
    val dndEnabled: Boolean = false,
    val dndAccessGranted: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class FocusSettingsViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val manager: FocusManager,
    private val protectedApps: ProtectedApps,
    private val musicApps: MusicApps,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val selectedId = MutableStateFlow<Long?>(null)
    private val protectedPackages = MutableStateFlow(emptySet<String>())
    private val musicPackages = MutableStateFlow(emptySet<String>())
    private val dndAccess = MutableStateFlow(hasDndAccess())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
            musicPackages.value = musicApps.packages()
        }
    }

    val uiState: StateFlow<FocusSettingsUiState> =
        combine(
            appsRepository.apps,
            manager.allProfiles,
            selectedId,
            protectedPackages,
            _query,
            musicPackages,
            dndAccess,
        ) { flows ->
            val apps = flows[0] as List<AppInfo>
            val profiles = flows[1] as List<FocusProfile>
            val sel = flows[2] as Long?
            @Suppress("UNCHECKED_CAST")
            val protected = flows[3] as Set<String>
            val query = flows[4] as String
            @Suppress("UNCHECKED_CAST")
            val music = flows[5] as Set<String>
            val granted = flows[6] as Boolean
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
                        allowed = app.packageName in allowed ||
                            app.packageName in protected ||
                            app.packageName in music,
                        isProtected = app.packageName in protected,
                        isMusic = app.packageName in music,
                    )
                },
                query = query,
                schedule = FocusSchedule.fromJson(selected?.scheduleJson),
                dndEnabled = selected?.dndEnabled == true,
                dndAccessGranted = granted,
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
        val state = uiState.value
        val id = state.selectedId ?: return
        if (app.packageName in protectedPackages.value) return
        if (app.packageName in musicPackages.value) return
        val selected = state.profiles.firstOrNull { it.id == id } ?: return
        val latest = FocusFilter.parseAllowed(selected.allowedAppsJson).toMutableSet()
        if (checked) latest.add(app.packageName) else latest.remove(app.packageName)
        viewModelScope.launch(Dispatchers.IO) { manager.setAllowedApps(id, latest) }
    }

    fun onDndToggle(enabled: Boolean) {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setDnd(id, enabled) }
    }

    fun refreshDndAccess() {
        dndAccess.value = hasDndAccess()
    }

    private fun hasDndAccess(): Boolean =
        context.getSystemService(NotificationManager::class.java)
            ?.isNotificationPolicyAccessGranted == true

    fun onSaveSchedule(schedule: FocusSchedule) {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setSchedule(id, schedule.toJson()) }
    }

    fun onClearSchedule() {
        val id = uiState.value.selectedId ?: return
        viewModelScope.launch { manager.setSchedule(id, null) }
    }
}
