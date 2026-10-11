package com.example.focuslauncher.domain.focus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusStateTest {

    @Test
    fun endsAt_addsMinutes() {
        assertEquals(1_000L + 25 * 60_000L, FocusDurations.endsAt(1_000L, 25))
    }

    @Test(expected = IllegalArgumentException::class)
    fun endsAt_rejectsNonPositive() {
        FocusDurations.endsAt(0L, 0)
    }

    @Test
    fun remaining_countsDownAndClamps() {
        val active = FocusState.Active(1, 2, 0L, 60_000L)
        assertEquals(59_000L, active.remaining(1_000L))
        assertEquals(0L, active.remaining(60_000L))
        assertEquals(0L, active.remaining(99_000L))
    }

    @Test
    fun isExpired_atAndAfterEnd() {
        val active = FocusState.Active(1, 2, 0L, 60_000L)
        assertFalse(active.isExpired(59_999L))
        assertTrue(active.isExpired(60_000L))
        assertTrue(active.isExpired(61_000L))
    }

    @Test
    fun presets_are25_50_90() {
        assertEquals(listOf(25, 50, 90), FocusDurations.presets)
    }

    @Test
    fun countdown_formatsMmSs() {
        val active = FocusState.Active(1, 2, 0L, (24 * 60 + 59) * 1000L)
        assertEquals("24:59", active.countdown(0L))
        assertEquals("00:00", active.countdown(25 * 60_000L))
    }
}
