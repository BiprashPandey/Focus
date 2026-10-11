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

    @Test
    fun overLimitPromotesToGated() {
        assertEquals(
            AppLevel.PAUSE_GATED,
            AppVisibility.effectiveLevel(AppLevel.VISIBLE, 45, 30, false),
        )
        assertEquals(
            AppLevel.VISIBLE,
            AppVisibility.effectiveLevel(AppLevel.VISIBLE, 10, 30, false),
        )
    }

    @Test
    fun overLimitLeavesProtectedAndGatedAlone() {
        assertEquals(AppLevel.VISIBLE, AppVisibility.effectiveLevel(AppLevel.HIDDEN, 99, 30, true))
        assertEquals(
            AppLevel.PAUSE_GATED,
            AppVisibility.effectiveLevel(AppLevel.PAUSE_GATED, 0, 30, false),
        )
    }

    @Test
    fun nullLimitOrUsageKeepsLevel() {
        assertEquals(AppLevel.VISIBLE, AppVisibility.effectiveLevel(AppLevel.VISIBLE, null, 30, false))
        assertEquals(AppLevel.TUCKED, AppVisibility.effectiveLevel(AppLevel.TUCKED, 99, null, false))
    }
}
