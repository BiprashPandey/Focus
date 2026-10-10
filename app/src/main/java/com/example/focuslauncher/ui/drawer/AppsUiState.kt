package com.example.focuslauncher.ui.drawer

import com.example.focuslauncher.domain.model.AppInfo

data class AppsUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
)
