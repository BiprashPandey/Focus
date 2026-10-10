package com.example.focuslauncher.domain.rules

import com.example.focuslauncher.domain.model.AppInfo
import java.text.Normalizer

/** Pure-Kotlin sorting/filtering for the app list. No Android dependencies. */
object AppListRules {

    fun sort(apps: List<AppInfo>): List<AppInfo> =
        apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

    /** Accent-insensitive, case-insensitive substring match on label or package. */
    fun filter(apps: List<AppInfo>, query: String): List<AppInfo> {
        val q = normalize(query).trim()
        if (q.isEmpty()) return apps
        return apps.filter {
            normalize(it.label).contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
}
