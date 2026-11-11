package com.example.focuslauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val CardSpacing = 12.dp
private val PageDotsHeight = 24.dp
private val EstimatedCardHeight = 180.dp

/**
 * Lays out home cards in left-to-right pages. Cards are measured at their
 * natural height, then packed so page 0 holds as many as fit; overflow flows
 * only to the right. Oversized cards get a vertically scrollable page.
 */
@Composable
fun HomeCardPages(
    cards: List<String>,
    modifier: Modifier = Modifier,
    onSwipeUp: () -> Unit = {},
    cardContent: @Composable (String) -> Unit,
) {
    if (cards.isEmpty()) return

    val density = LocalDensity.current
    val heights = remember { mutableStateMapOf<String, Int>() }
    val scope = rememberCoroutineScope()
    val currentOnSwipeUp by rememberUpdatedState(onSwipeUp)
    val swipeThreshold = with(density) { 72.dp.toPx() }
    val drawerSwipe = remember(swipeThreshold) {
        object : NestedScrollConnection {
            private var accumulated = 0f

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (available.y < 0f) {
                    accumulated += -available.y
                    if (accumulated >= swipeThreshold) {
                        accumulated = 0f
                        currentOnSwipeUp()
                    }
                } else if (available.y > 0f) {
                    accumulated = 0f
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(cards) {
        val current = cards.toSet()
        heights.keys.retainAll(current)
    }

    BoxWithConstraints(
        modifier = modifier.nestedScroll(drawerSwipe),
    ) {
        val spacingPx = with(density) { CardSpacing.roundToPx() }
        val dotsPx = with(density) { PageDotsHeight.roundToPx() }
        val estimatedPx = with(density) { EstimatedCardHeight.roundToPx() }
        val pageHeightPx = (constraints.maxHeight - dotsPx).coerceAtLeast(1)
        val measured = cards.map { heights[it] ?: estimatedPx }
        val pages = if (heights.isEmpty()) {
            listOf(cards.indices.toList())
        } else {
            CardPagePacker.pack(measured, pageHeightPx, spacingPx)
        }
        val pagerState = rememberPagerState(pageCount = { pages.size })

        LaunchedEffect(pages.size) {
            if (pagerState.currentPage > pages.lastIndex) {
                pagerState.scrollToPage(pages.lastIndex)
            }
        }
        BackHandler(enabled = pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(0) }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                pageSpacing = 8.dp,
                beyondViewportPageCount = 1,
            ) { pageIndex ->
                val pageCards = pages.getOrNull(pageIndex)
                    ?.mapNotNull(cards::getOrNull)
                    .orEmpty()
                key(pageCards.joinToString("|")) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(CardSpacing),
                    ) {
                        pageCards.forEach { card ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onSizeChanged { heights[card] = it.height },
                            ) {
                                cardContent(card)
                            }
                        }
                    }
                }
            }
            if (pages.size > 1) {
                PageDots(
                    count = pages.size,
                    current = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth().height(PageDotsHeight),
                )
            } else {
                Spacer(modifier = Modifier.height(PageDotsHeight))
            }
        }
    }
}

@Composable
private fun PageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .size(if (selected) 7.dp else 5.dp)
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        },
                        shape = CircleShape,
                    ),
            )
            if (index != count - 1) {
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}
