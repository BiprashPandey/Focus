package com.example.focuslauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.apps.AppsRepository
import com.example.focuslauncher.data.dock.DockRepository
import com.example.focuslauncher.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(appsRepository.apps, dockRepository.dockKeys) { apps, keys ->
            HomeUiState(
                allApps = apps,
                dock = DockRepository.resolveDock(keys, apps),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun launch(app: AppInfo): Boolean = appsRepository.launch(app)

    fun setDockSlot(index: Int, app: AppInfo?) {
        viewModelScope.launch { dockRepository.setSlot(index, app?.key) }
    }
}
