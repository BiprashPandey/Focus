package com.example.focuslauncher.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.util.LruCache
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.rules.AppListRules
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface AppsRepository {
    val apps: StateFlow<List<AppInfo>>

    /** Lazily loads and caches the icon; safe to call from any thread. */
    suspend fun loadIcon(app: AppInfo): Drawable?
}

/**
 * Lists launchable activities via [LauncherApps] (supports work profiles) and
 * refreshes live through [LauncherApps.Callback]. All I/O stays off main.
 */
@Singleton
class LauncherAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val launcherApps: LauncherApps,
) : AppsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    override val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val infoByKey = ConcurrentHashMap<String, android.content.pm.LauncherActivityInfo>()
    private val iconCache = object : LruCache<String, Drawable>(ICON_CACHE_SIZE) {}

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refresh()
        override fun onPackagesAvailable(
            packageNames: Array<String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()
        override fun onPackagesUnavailable(
            packageNames: Array<String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()
    }

    init {
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        refresh()
    }

    private fun refresh() {
        scope.launch { _apps.value = queryAll() }
    }

    private fun queryAll(): List<AppInfo> {
        val out = ArrayList<AppInfo>()
        infoByKey.clear()
        for (user in launcherApps.profiles) {
            for (info in launcherApps.getActivityList(null, user)) {
                val app = AppInfo(
                    label = info.label.toString(),
                    packageName = info.componentName.packageName,
                    componentFlattened = info.componentName.flattenToString(),
                    userKey = user.hashCode().toString(),
                )
                infoByKey[app.key] = info
                out.add(app)
            }
        }
        return AppListRules.sort(out)
    }

    override suspend fun loadIcon(app: AppInfo): Drawable? = withContext(Dispatchers.IO) {
        iconCache.get(app.key)
            ?: infoByKey[app.key]?.getIcon(0)?.also { iconCache.put(app.key, it) }
    }

    companion object {
        private const val ICON_CACHE_SIZE = 64
    }
}
