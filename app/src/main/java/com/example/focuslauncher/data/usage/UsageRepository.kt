package com.example.focuslauncher.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface UsageRepository {
    /** True if the user granted usage-access special permission. */
    fun hasPermission(): Boolean

    /** Foreground minutes per package since midnight. Empty map when no permission. */
    suspend fun todayMinutes(): Map<String, Int>
}

@Singleton
class SystemUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageStats: UsageStatsManager,
    private val appOps: AppOpsManager,
) : UsageRepository {

    override fun hasPermission(): Boolean = try {
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        mode == AppOpsManager.MODE_ALLOWED
    } catch (_: SecurityException) {
        false
    }

    override suspend fun todayMinutes(): Map<String, Int> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyMap()
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val stats = try {
            usageStats.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, now)
        } catch (_: SecurityException) {
            return@withContext emptyMap()
        } ?: return@withContext emptyMap()
        val out = HashMap<String, Long>()
        for (s in stats) {
            out[s.packageName] = (out[s.packageName] ?: 0L) + s.totalTimeInForeground
        }
        out.mapValues { (it.value / 60_000L).toInt() }
    }
}
