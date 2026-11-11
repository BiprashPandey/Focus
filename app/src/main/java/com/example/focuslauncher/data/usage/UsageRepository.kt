package com.example.focuslauncher.data.usage

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
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

    override fun hasPermission(): Boolean =
        appOpsAllowed() || selfPermissionGranted() || recentStatsAvailable()

    /**
     * Some OEMs (notably MIUI and HONOR) keep reporting MODE_DEFAULT through
     * AppOps even after the user grants usage access. The permission check and
     * a live stats probe cover those ROMs.
     */
    private fun appOpsAllowed(): Boolean = try {
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        mode == AppOpsManager.MODE_ALLOWED
    } catch (_: SecurityException) {
        false
    }

    /** Recommended fallback when AppOps reports MODE_DEFAULT. */
    private fun selfPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.PACKAGE_USAGE_STATS,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * A granted permission always produces at least one stats entry or event.
     * The list is accepted even when every entry reports zero foreground time,
     * because some ROMs aggregate totals lazily.
     */
    private fun recentStatsAvailable(): Boolean {
        val now = System.currentTimeMillis()
        return try {
            val daily = usageStats.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - PERMISSION_PROBE_WINDOW_MS,
                now,
            )
            if (!daily.isNullOrEmpty()) return true
            val best = usageStats.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                now - PERMISSION_PROBE_WINDOW_MS,
                now,
            )
            if (!best.isNullOrEmpty()) return true
            val events = usageStats.queryEvents(now - PERMISSION_EVENT_WINDOW_MS, now)
            events?.getNextEvent(android.app.usage.UsageEvents.Event()) == true
        } catch (_: SecurityException) {
            false
        }
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

    private companion object {
        const val PERMISSION_PROBE_WINDOW_MS = 7L * 24 * 60 * 60 * 1_000
        const val PERMISSION_EVENT_WINDOW_MS = 24L * 60 * 60 * 1_000
    }
}
