package com.example.focuslauncher.data.focus

import android.app.NotificationManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional Do Not Disturb during focus. Only touched when the user turns the
 * option on for a profile; restoring the previous mode on session end.
 * Phone calls and alarms still ring (INTERRUPTION_FILTER_ALARMS).
 */
@Singleton
class DndController @Inject constructor() {

    private var touched = false

    fun hasAccess(nm: NotificationManager): Boolean = nm.isNotificationPolicyAccessGranted

    fun enable(nm: NotificationManager) {
        if (!hasAccess(nm)) return
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        touched = true
    }

    fun restore(nm: NotificationManager) {
        if (!touched || !hasAccess(nm)) return
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        touched = false
    }
}
