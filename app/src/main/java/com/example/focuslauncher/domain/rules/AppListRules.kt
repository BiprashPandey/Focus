package com.example.focuslauncher.domain.rules

import com.example.focuslauncher.domain.model.AppInfo
import java.text.Normalizer

/** Pure-Kotlin sorting/filtering for the app list. No Android dependencies. */
object AppListRules {

    fun sort(apps: List<AppInfo>): List<AppInfo> =
        apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

    /**
     * Accent-insensitive match on label or package. Direct substring hits rank
     * first (alphabetical), then subsequence ("fuzzy") hits on the label.
     */
    fun filter(apps: List<AppInfo>, query: String): List<AppInfo> {
        val q = normalize(query).trim()
        if (q.isEmpty()) return apps
        val direct = ArrayList<AppInfo>()
        val fuzzy = ArrayList<AppInfo>()
        for (app in apps) {
            val label = normalize(app.label)
            if (label.contains(q) || app.packageName.lowercase().contains(q)) {
                direct.add(app)
            } else if (isSubsequence(label, q)) {
                fuzzy.add(app)
            }
        }
        return sort(direct) + sort(fuzzy)
    }

    /**
     * HIDDEN apps are search-only: shown only when the query equals the full
     * app label (accent- and case-insensitive). Partial matches stay hidden.
     */
    fun matchesFullName(label: String, query: String): Boolean {
        val q = normalize(query).trim()
        if (q.isEmpty()) return false
        return normalize(label).trim() == q
    }

    /** Returns the subset of [hidden] whose label fully matches [query]. */
    fun filterHiddenFullNameOnly(hidden: List<AppInfo>, query: String): List<AppInfo> {
        val q = normalize(query).trim()
        if (q.isEmpty()) return emptyList()
        return hidden.filter { normalize(it.label).trim() == q }
    }

    private fun isSubsequence(text: String, query: String): Boolean {
        var qi = 0
        for (c in text) {
            if (c == query[qi]) {
                qi++
                if (qi == query.length) return true
            }
        }
        return false
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
}
