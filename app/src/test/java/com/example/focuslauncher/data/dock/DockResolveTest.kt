package com.example.focuslauncher.data.dock

import com.example.focuslauncher.domain.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DockResolveTest {

    private fun app(label: String) = AppInfo(
        label = label,
        packageName = "com.example.$label",
        componentFlattened = "com.example.$label/.Main",
        userKey = "0",
    )

    @Test
    fun resolveDock_mapsKeysAndKeepsEmptySlots() {
        val camera = app("Camera")
        val clock = app("Clock")
        val keys = listOf(camera.key, null, "gone/.Main#0", clock.key)
        val dock = DockRepository.resolveDock(keys, listOf(camera, clock))
        assertEquals(camera, dock[0])
        assertNull(dock[1])
        assertNull(dock[2])
        assertEquals(clock, dock[3])
    }

    @Test
    fun resolveDock_shortKeyListPadsWithNulls() {
        val dock = DockRepository.resolveDock(emptyList(), listOf(app("Camera")))
        assertEquals(DockRepository.DOCK_SIZE, dock.size)
        assertEquals(listOf(null, null, null, null), dock)
    }
}
