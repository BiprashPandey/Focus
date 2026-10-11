package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.AppConfigRepository
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.data.focus.FocusProfile
import com.example.focuslauncher.domain.focus.FocusFilter
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.domain.rules.AppListRules
import com.example.focuslauncher.domain.rules.AppVisibility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
    private val appConfigRepository: AppConfigRepository,
    private val protectedApps: ProtectedApps,
    private val focusManager: FocusManager,
    private val usageRepository: com.example.focuslauncher.data.usage.UsageRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val protectedPackages = MutableStateFlow(emptySet<String>())
    private val usageMinutes = MutableStateFlow(emptyMap<String, Int>())
    private val hasUsagePermission = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
            refreshUsage()
        }
    }

    /** Re-query usage stats; safe to call even without permission (no-op). */
    fun refreshUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            hasUsagePermission.value = usageRepository.hasPermission()
            usageMinutes.value = try {
                usageRepository.todayMinutes()
            } catch (_: SecurityException) {
                emptyMap()
            }
        }
    }

    val uiState: StateFlow<AppsUiState> =
        combine(
            repository.apps,
            appConfigRepository.configs,
            protectedPackages,
            usageMinutes,
            hasUsagePermission,
            focusManager.state,
            focusManager.allProfiles,
            _query,
        ) { flows ->
            val apps = flows[0] as List<AppInfo>
            @Suppress("UNCHECKED_CAST")
            val configs = flows[1] as Map<String, com.example.focuslauncher.domain.model.AppConfig>
            @Suppress("UNCHECKED_CAST")
            val protected = flows[2] as Set<String>
            @Suppress("UNCHECKED_CAST")
            val usage = flows[3] as Map<String, Int>
            val hasPermission = flows[4] as Boolean
            val focusState = flows[5] as FocusState
            @Suppress("UNCHECKED_CAST")
            val profiles = flows[6] as List<FocusProfile>
            val query = flows[7] as String
            // During focus only allowed + protected apps survive; the rest are
            // blocked (launching routes to the pause screen).
            val active = focusState as? FocusState.Active
            val allowed = active?.let { a ->
                profiles.firstOrNull { it.id == a.profileId }
                    ?.let { FocusFilter.parseAllowed(it.allowedAppsJson) }
            }
            val inFocus = active != null
            val candidates = if (allowed != null) {
                FocusFilter.visible(apps, allowed, protected)
            } else {
                apps
            }
            val blocked = if (allowed != null) {
                FocusFilter.blockedPackages(apps, allowed, protected)
            } else {
                emptySet()
            }
            val levels: Map<String, AppLevel> = buildMap {
                for (app in apps) {
                    val cfg = configs[app.packageName]
                    put(
                        app.packageName,
                        AppVisibility.effectiveLevel(
                            configured = cfg?.level ?: AppLevel.VISIBLE,
                            todayMin = usage[app.packageName],
                            limitMin = cfg?.dailyLimitMin,
                            isProtected = app.packageName in protected,
                        ),
                    )
                }
            }
            val grouped = AppVisibility.group(candidates, levels, protected)
            val main = AppListRules.filter(grouped.visible + grouped.gated, query)
            val hiddenMatch = AppListRules.filterHiddenFullNameOnly(grouped.hidden, query)
            val tucked = AppListRules.filter(grouped.tucked, query)
            AppsUiState(
                apps = main + hiddenMatch,
                tucked = tucked,
                gatedPackages = grouped.gated.map { it.packageName }.toSet(),
                blockedPackages = blocked,
                inFocus = inFocus,
                protectedPackages = protected,
                usageMinutes = usage,
                hasUsagePermission = hasPermission,
                isLoading = false,
                query = query,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState(isLoading = true))

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun setLevel(app: AppInfo, level: AppLevel) {
        if (app.packageName in protectedPackages.value) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appConfigRepository.setLevel(app.packageName, level)
            }
        }
    }

    suspend fun loadIcon(app: AppInfo): Drawable? = repository.loadIcon(app)

    fun launch(app: AppInfo): Boolean = repository.launch(app)
}
