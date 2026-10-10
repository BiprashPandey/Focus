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
