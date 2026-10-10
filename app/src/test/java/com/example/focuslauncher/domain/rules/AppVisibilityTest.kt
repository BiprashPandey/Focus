package com.example.focuslauncher.domain.rules

import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class AppVisibilityTest {

    private fun app(pkg: String) = AppInfo(
        label = pkg,
        packageName = pkg,
        componentFlattened = "$pkg/.Main",
        userKey = "0",
    )

    @Test
    fun unconfiguredAppsAreVisible() {
        val apps = listOf(app("a"), app("b"))
        val grouped = AppVisibility.group(apps, emptyMap(), emptySet())
        assertEquals(apps, grouped.visible)
        assertEquals(emptyList<AppInfo>(), grouped.tucked)
        assertEquals(emptyList<AppInfo>(), grouped.hidden)
        assertEquals(emptyList<AppInfo>(), grouped.gated)
    }

    @Test
    fun tuckedAppsLandInTuckedGroup() {
        val apps = listOf(app("a"), app("b"))
        val grouped = AppVisibility.group(apps, mapOf("b" to AppLevel.TUCKED), emptySet())
        assertEquals(listOf(app("a")), grouped.visible)
        assertEquals(listOf(app("b")), grouped.tucked)
    }

    @Test
    fun hiddenAppsLandInHiddenGroup() {
        val apps = listOf(app("a"), app("b"))
        val grouped = AppVisibility.group(apps, mapOf("a" to AppLevel.HIDDEN), emptySet())
        assertEquals(listOf(app("b")), grouped.visible)
        assertEquals(listOf(app("a")), grouped.hidden)
    }

    @Test
    fun gatedAppsLandInGatedGroup() {
        val apps = listOf(app("a"), app("b"))
        val grouped = AppVisibility.group(apps, mapOf("a" to AppLevel.PAUSE_GATED), emptySet())
        assertEquals(listOf(app("b")), grouped.visible)
        assertEquals(listOf(app("a")), grouped.gated)
    }

    @Test
    fun protectedPackagesStayVisibleAtAnyLevel() {
        val apps = listOf(app("phone"), app("settings"), app("game"))
        val levels = mapOf(
            "phone" to AppLevel.HIDDEN,
            "settings" to AppLevel.PAUSE_GATED,
            "game" to AppLevel.TUCKED,
        )
        val grouped = AppVisibility.group(apps, levels, setOf("phone", "settings"))
        assertEquals(listOf(app("phone"), app("settings")), grouped.visible)
        assertEquals(listOf(app("game")), grouped.tucked)
        assertEquals(emptyList<AppInfo>(), grouped.hidden)
        assertEquals(emptyList<AppInfo>(), grouped.gated)
    }
}
