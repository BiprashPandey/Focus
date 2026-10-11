package com.example.focuslauncher.domain.rules

import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel

/** Apps partitioned by level. Input order (alphabetical) is preserved in each group. */
data class GroupedApps(
    val visible: List<AppInfo> = emptyList(),
    val tucked: List<AppInfo> = emptyList(),
    val hidden: List<AppInfo> = emptyList(),
    val gated: List<AppInfo> = emptyList(),
)

/** Pure-Kotlin level rules. Protected packages are always VISIBLE. */
object AppVisibility {

    /**
     * Effective level after daily limits: when usage access is available and
     * [todayMin] reaches [limitMin], a VISIBLE/TUCKED/HIDDEN app is promoted
     * to PAUSE_GATED for the rest of the day. Protected packages and already
     * gated apps are unchanged. Null limit or missing permission (null
     * [todayMin]) leaves the level unchanged.
     */
    fun effectiveLevel(
        configured: AppLevel,
        todayMin: Int?,
        limitMin: Int?,
        isProtected: Boolean,
    ): AppLevel {
        if (isProtected) return AppLevel.VISIBLE
        if (configured == AppLevel.PAUSE_GATED) return configured
        if (limitMin == null || todayMin == null) return configured
        return if (todayMin >= limitMin) AppLevel.PAUSE_GATED else configured
    }


    fun group(
        apps: List<AppInfo>,
        levels: Map<String, AppLevel>,
        protectedPackages: Set<String>,
    ): GroupedApps {
        val visible = ArrayList<AppInfo>()
        val tucked = ArrayList<AppInfo>()
        val hidden = ArrayList<AppInfo>()
        val gated = ArrayList<AppInfo>()
        for (app in apps) {
            val level = if (app.packageName in protectedPackages) {
                AppLevel.VISIBLE
            } else {
                levels[app.packageName] ?: AppLevel.VISIBLE
            }
            when (level) {
                AppLevel.VISIBLE -> visible.add(app)
                AppLevel.TUCKED -> tucked.add(app)
                AppLevel.HIDDEN -> hidden.add(app)
                AppLevel.PAUSE_GATED -> gated.add(app)
            }
        }
        return GroupedApps(visible = visible, tucked = tucked, hidden = hidden, gated = gated)
    }
}
