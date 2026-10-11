package com.example.focuslauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.dock.DockRepository
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.domain.focus.FocusFilter
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val dockRepository: DockRepository,
    private val focusManager: FocusManager,
    private val protectedApps: ProtectedApps,
) : ViewModel() {

    private val protectedPackages = MutableStateFlow(emptySet<String>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            appsRepository.apps,
            dockRepository.dockKeys,
            focusManager.state,
            focusManager.allProfiles,
            protectedPackages,
        ) { apps, keys, focusState, profiles, protected ->
            val active = focusState as? FocusState.Active
            val visible = if (active != null) {
                val allowed = profiles.firstOrNull { it.id == active.profileId }
                    ?.let { FocusFilter.parseAllowed(it.allowedAppsJson) }
                    ?: emptySet()
                FocusFilter.visible(apps, allowed, protected)
            } else {
                apps
            }
            HomeUiState(
                allApps = visible,
                dock = DockRepository.resolveDock(keys, visible),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun launch(app: AppInfo): Boolean = appsRepository.launch(app)

    fun setDockSlot(index: Int, app: AppInfo?) {
        viewModelScope.launch { dockRepository.setSlot(index, app?.key) }
    }
}
