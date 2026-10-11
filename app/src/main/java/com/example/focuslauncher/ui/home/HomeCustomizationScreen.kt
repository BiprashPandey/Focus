package com.example.focuslauncher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.settings.HomeCards
import com.example.focuslauncher.data.settings.HomeLayout
import com.example.focuslauncher.data.settings.Layouts

@Composable
fun HomeCustomizationScreen(
    layout: HomeLayout,
    onMove: (String, Boolean) -> Unit,
    onHidden: (String, Boolean) -> Unit,
    onLayout: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_customize_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.manage_apps_close),
                        )
                    }
                }
            }
            item(key = "layout") {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = layout.layout == Layouts.DASHBOARD,
                        onClick = { onLayout(Layouts.DASHBOARD) },
                        label = { Text(stringResource(R.string.home_layout_dashboard)) },
                    )
                    FilterChip(
                        selected = layout.layout == Layouts.MINIMAL,
                        onClick = { onLayout(Layouts.MINIMAL) },
                        label = { Text(stringResource(R.string.home_layout_minimal)) },
                    )
                }
                HorizontalDivider()
            }
            val order = layout.order.filter { it in HomeCards.defaultOrder } +
                HomeCards.defaultOrder.filter { it !in layout.order }
            items(order, key = { it }) { card ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = card !in layout.hidden,
                        onCheckedChange = { onHidden(card, !it) },
                    )
                    Text(
                        text = cardName(card),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onMove(card, true) }) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.home_move_up),
                        )
                    }
                    IconButton(onClick = { onMove(card, false) }) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.home_move_down),
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun cardName(card: String): String = stringResource(
    when (card) {
        HomeCards.MOTIVATION -> R.string.intention_title
        HomeCards.TODOS -> R.string.todos_title
        HomeCards.FOCUS -> R.string.focus_title
        HomeCards.NOTES -> R.string.notes_title
        else -> R.string.calendar_title
    },
)
