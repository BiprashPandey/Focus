package com.example.focuslauncher.domain.rules

import com.example.focuslauncher.domain.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class AppListRulesTest {

    private fun app(label: String, pkg: String = "com.example.$label") = AppInfo(
        label = label,
        packageName = pkg,
        componentFlattened = "$pkg/.Main",
        userKey = "0",
    )

    @Test
    fun sort_isCaseInsensitiveAlphabetical() {
        val input = listOf(app("gamma"), app("Alpha"), app("beta"))
        assertEquals(
            listOf("Alpha", "beta", "gamma"),
            AppListRules.sort(input).map { it.label },
        )
    }

    @Test
    fun filter_matchesAccentsAndCase() {
        val apps = listOf(app("Café Reader"), app("Gallery"), app("Settings"))
        val result = AppListRules.filter(apps, "cafe")
        assertEquals(listOf("Café Reader"), result.map { it.label })
    }

    @Test
    fun filter_matchesPackageName() {
        val apps = listOf(app("Files", "com.android.documentsui"), app("Clock", "com.android.deskclock"))
        val result = AppListRules.filter(apps, "documentsui")
        assertEquals(listOf("Files"), result.map { it.label })
    }

    @Test
    fun filter_blankQueryReturnsAll() {
        val apps = listOf(app("One"), app("Two"))
        assertEquals(apps, AppListRules.filter(apps, "   "))
    }

    @Test
    fun filter_noMatchReturnsEmpty() {
        val apps = listOf(app("One"), app("Two"))
        assertEquals(emptyList<AppInfo>(), AppListRules.filter(apps, "zzz"))
    }

    @Test
    fun appKey_isUniquePerComponentAndUser() {
        val a = app("Mail", "com.mail").copy(componentFlattened = "com.mail/.A", userKey = "0")
        val b = a.copy(userKey = "10")
        val c = a.copy(componentFlattened = "com.mail/.B")
        assertEquals(3, setOf(a.key, b.key, c.key).size)
    }
}
