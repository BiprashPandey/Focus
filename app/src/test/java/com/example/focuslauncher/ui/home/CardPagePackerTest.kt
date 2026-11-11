package com.example.focuslauncher.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class CardPagePackerTest {

    @Test
    fun `empty cards produce no pages`() {
        assertEquals(emptyList<List<Int>>(), CardPagePacker.pack(emptyList(), 500, 10))
    }

    @Test
    fun `cards that fit stay on one page`() {
        val pages = CardPagePacker.pack(listOf(100, 150, 100), 500, 10)
        assertEquals(listOf(listOf(0, 1, 2)), pages)
    }

    @Test
    fun `spacing counts toward page height`() {
        val pages = CardPagePacker.pack(listOf(100, 100, 100), 250, 20)
        assertEquals(listOf(listOf(0, 1), listOf(2)), pages)
    }

    @Test
    fun `overflow moves cards right in order`() {
        val pages = CardPagePacker.pack(listOf(300, 300, 100), 500, 12)
        assertEquals(listOf(listOf(0), listOf(1, 2)), pages)
    }

    @Test
    fun `oversized card gets its own page`() {
        val pages = CardPagePacker.pack(listOf(700, 100, 100), 500, 12)
        assertEquals(listOf(listOf(0), listOf(1, 2)), pages)
    }

    @Test
    fun `non-positive page height falls back to one page`() {
        val pages = CardPagePacker.pack(listOf(100, 200), 0, 12)
        assertEquals(listOf(listOf(0, 1)), pages)
    }
}
