package com.example.focuslauncher.data.appconfig

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Packages the user can never hide: this launcher, Settings, the default
 * dialer, the default SMS app, and the default clock app. Resolved
 * dynamically so OEM renames (Samsung/MIUI dialers, clocks) just work.
 */
@Singleton
class ProtectedApps @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun resolve(): Set<String> {
        val out = mutableSetOf(context.packageName, SETTINGS_PACKAGE)
        context.getSystemService(android.telecom.TelecomManager::class.java)
            ?.defaultDialerPackage?.let { out.add(it) }
        Telephony.Sms.getDefaultSmsPackage(context)?.let { out.add(it) }
        clockPackage()?.let { out.add(it) }
        return out
    }

    private fun clockPackage(): String? {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, 0)
        }
        return resolved?.activityInfo?.packageName
    }

    companion object {
        const val SETTINGS_PACKAGE = "com.android.settings"
    }
}
