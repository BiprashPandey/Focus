package com.example.focuslauncher.data.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * After a reboot, restore a running focus session (or close an expired one)
 * so the timer stays correct. The manager reads the open session row.
 */
@AndroidEntryPoint
class FocusBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var focusManager: FocusManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_TIMEZONE_CHANGED
        ) {
            return
        }
        val pending = goAsync()
        scope.launch {
            try {
                focusManager.refresh()
            } finally {
                pending.finish()
            }
        }
    }
}
