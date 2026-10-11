package com.example.focuslauncher.ui.drawer

import com.example.focuslauncher.domain.model.AppInfo

data class AppsUiState(
    val apps: List<AppInfo> = emptyList(),
    val tucked: List<AppInfo> = emptyList(),
    val gatedPackages: Set<String> = emptySet(),
    val protectedPackages: Set<String> = emptySet(),
    val usageMinutes: Map<String, Int> = emptyMap(),
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = false,
    val query: String = "",
)
