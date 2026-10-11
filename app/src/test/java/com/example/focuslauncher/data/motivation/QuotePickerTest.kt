package com.example.focuslauncher.data.motivation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotePickerTest {

    private val quotes = listOf(
        Quote("general one", "A", "general"),
        Quote("general two", "A", "general"),
        Quote("coach one", "B", "coach"),
    )

    @Test
    fun parse_readsEntries() {
        val json = """[{"t":"Hi","b":"Me","tone":"general"}, {"t":"Go","b":"You","tone":"coach"}]"""
        val out = QuotePicker.parse(json)
        assertEquals(2, out.size)
        assertEquals(Quote("Hi", "Me", "general"), out[0])
    }

    @Test
    fun parse_ignoresGarbage() {
        assertTrue(QuotePicker.parse("not json").isEmpty())
    }

    @Test
    fun pick_prefersToneThenFallsBack() {
        assertEquals("coach one", QuotePicker.pick(quotes, "coach", 0L)?.text)
        // No stoic entries -> general pool, rotating by day.
        assertEquals("general one", QuotePicker.pick(quotes, "stoic", 0L)?.text)
        assertEquals("general two", QuotePicker.pick(quotes, "stoic", 1L)?.text)
    }

    @Test
    fun pick_emptyReturnsNull() {
        assertNull(QuotePicker.pick(emptyList(), "coach", 0L))
    }

    @Test
    fun nudge_capAndQuietHours() {
        assertEquals(true, NudgePolicy.shouldShow(0, 9))
        assertEquals(true, NudgePolicy.shouldShow(2, 21))
        assertEquals(false, NudgePolicy.shouldShow(3, 9))
        assertEquals(false, NudgePolicy.shouldShow(0, 22))
        assertEquals(false, NudgePolicy.shouldShow(0, 6))
        assertEquals(true, NudgePolicy.shouldShow(0, 7))
    }
}
