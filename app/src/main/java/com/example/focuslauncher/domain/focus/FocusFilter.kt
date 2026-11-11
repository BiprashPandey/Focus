package com.example.focuslauncher.domain.focus

import com.example.focuslauncher.domain.model.AppInfo

/**
 * Pure-Kotlin focus filtering. During a session only the profile's allowed
 * apps (plus never-blocked essentials) stay visible; everything else is
 * treated as blocked and opens the pause screen instead.
 */
object FocusFilter {

    /**
     * Apps visible during focus: allowed packages + protected essentials +
     * [alwaysAllowed] extras (for example music players).
     */
    fun visible(
        apps: List<AppInfo>,
        allowedPackages: Set<String>,
        protectedPackages: Set<String>,
        alwaysAllowed: Set<String> = emptySet(),
    ): List<AppInfo> = apps.filter {
        it.packageName in allowedPackages ||
            it.packageName in protectedPackages ||
            it.packageName in alwaysAllowed
    }

    /** Packages hidden during focus (informational; launching routes to pause). */
    fun blockedPackages(
        apps: List<AppInfo>,
        allowedPackages: Set<String>,
        protectedPackages: Set<String>,
        alwaysAllowed: Set<String> = emptySet(),
    ): Set<String> = apps
        .map { it.packageName }
        .filter {
            it !in allowedPackages &&
                it !in protectedPackages &&
                it !in alwaysAllowed
        }
        .toSet()

    /** Parses `["pkg.a","pkg.b"]` (tolerant to spaces). Returns empty on garbage. */
    fun parseAllowed(json: String): Set<String> {
        val t = json.trim()
        if (!t.startsWith("[") || !t.endsWith("]")) return emptySet()
        return t.substring(1, t.length - 1)
            .split(",")
            .map { it.trim().trim('"') }
            .filter { it.isNotEmpty() && !it.contains(Regex("\\s")) }
            .toSet()
    }

    fun encodeAllowed(packages: Set<String>): String =
        packages.sorted().joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
