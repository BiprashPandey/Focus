package com.example.focuslauncher.domain.model

/**
 * One launchable activity for one user profile. Pure Kotlin: no Android types,
 * so sorting/filtering rules stay unit-testable on the JVM.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val componentFlattened: String,
    val userKey: String,
) {
    /** Stable key for LazyColumn items and icon-cache lookups. */
    val key: String get() = "$componentFlattened#$userKey"
}
