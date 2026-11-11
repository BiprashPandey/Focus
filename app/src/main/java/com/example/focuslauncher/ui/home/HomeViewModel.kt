package com.example.focuslauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.appconfig.ProtectedApps
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.apps.MusicApps
import com.example.focuslauncher.data.dock.DockRepository
import com.example.focuslauncher.data.focus.FocusManager
import com.example.focuslauncher.data.focus.FocusProfile
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
    private val musicApps: MusicApps,
) : ViewModel() {

    private val protectedPackages = MutableStateFlow(emptySet<String>())
    private val musicPackages = MutableStateFlow(emptySet<String>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            protectedPackages.value = protectedApps.resolve()
            musicPackages.value = musicApps.packages()
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            appsRepository.apps,
            dockRepository.dockKeys,
            focusManager.state,
            focusManager.allProfiles,
            protectedPackages,
            musicPackages,
        ) { flows ->
            val apps = flows[0] as List<AppInfo>
            @Suppress("UNCHECKED_CAST")
            val keys = flows[1] as List<String?>
            val focusState = flows[2] as FocusState
            @Suppress("UNCHECKED_CAST")
            val profiles = flows[3] as List<FocusProfile>
            @Suppress("UNCHECKED_CAST")
            val protected = flows[4] as Set<String>
            @Suppress("UNCHECKED_CAST")
            val music = flows[5] as Set<String>
            val active = focusState as? FocusState.Active
            val visible = if (active != null) {
                val allowed = profiles.firstOrNull { it.id == active.profileId }
                    ?.let { FocusFilter.parseAllowed(it.allowedAppsJson) }
                    ?: emptySet()
                FocusFilter.visible(apps, allowed, protected, music)
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
