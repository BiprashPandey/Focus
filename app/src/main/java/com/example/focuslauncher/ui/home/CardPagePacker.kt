package com.example.focuslauncher.ui.home

/** Packs card heights into ordered pages without leaving gaps between cards. */
object CardPagePacker {
    fun pack(cardHeights: List<Int>, pageHeight: Int, spacing: Int): List<List<Int>> {
        if (cardHeights.isEmpty()) return emptyList()
        if (pageHeight <= 0) return listOf(cardHeights.indices.toList())

        val gap = spacing.coerceAtLeast(0)
        val pages = mutableListOf<List<Int>>()
        var current = mutableListOf<Int>()
        var used = 0

        cardHeights.forEachIndexed { index, height ->
            val next = if (current.isEmpty()) height else used + gap + height
            if (current.isNotEmpty() && next > pageHeight) {
                pages += current
                current = mutableListOf()
                used = 0
            }
            if (current.isNotEmpty()) used += gap
            current += index
            used += height.coerceAtLeast(0)
        }
        if (current.isNotEmpty()) pages += current
        return pages
    }
}
