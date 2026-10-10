package com.example.focuslauncher.ui.home

import com.example.focuslauncher.domain.model.AppInfo

data class HomeUiState(
    val allApps: List<AppInfo> = emptyList(),
    val dock: List<AppInfo?> = List(4) { null },
)
