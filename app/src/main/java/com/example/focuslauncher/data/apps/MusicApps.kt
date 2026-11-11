package com.example.focuslauncher.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the music players that stay available during a focus session.
 * CATEGORY_APP_MUSIC covers most devices; the known-package list covers OEMs
 * (notably MIUI) whose bundled player does not answer that category.
 */
@Singleton
class MusicApps @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Volatile
    private var cached: Set<String>? = null

    fun packages(): Set<String> = cached ?: resolve().also { cached = it }

    private fun resolve(): Set<String> {
        val pm = context.packageManager
        val out = linkedSetOf<String>()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
        try {
            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
            resolved.forEach { info ->
                info.activityInfo?.packageName?.let(out::add)
            }
        } catch (_: Exception) {
            // Fall through to the known-package list.
        }
        KNOWN_PLAYERS.filterTo(out) { isInstalled(pm, it) }
        return out
    }

    private fun isInstalled(pm: PackageManager, packageName: String): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private companion object {
        val KNOWN_PLAYERS = setOf(
            "com.miui.player",
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.google.android.music",
            "com.amazon.mp3",
            "com.soundcloud.android",
            "com.deezer.android",
            "com.apple.android.music",
            "com.aspiro.tidal",
        )
    }
}
