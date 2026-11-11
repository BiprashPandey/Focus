package com.example.focuslauncher.ui.drawer

import com.example.focuslauncher.domain.model.AppInfo

data class AppsUiState(
    val apps: List<AppInfo> = emptyList(),
    val tucked: List<AppInfo> = emptyList(),
    /** Locked apps; the drawer renders these after a device-credential unlock. */
    val hidden: List<AppInfo> = emptyList(),
    val gatedPackages: Set<String> = emptySet(),
    val blockedPackages: Set<String> = emptySet(),
    val inFocus: Boolean = false,
    val protectedPackages: Set<String> = emptySet(),
    val usageMinutes: Map<String, Int> = emptyMap(),
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = false,
    val query: String = "",
)
