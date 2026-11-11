package com.example.focuslauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.data.settings.HomeCards
import com.example.focuslauncher.data.settings.HomeLayout
import com.example.focuslauncher.data.settings.Layouts
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.GroupDivider
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.SettingsGroup
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

@Composable
fun HomeCustomizationScreen(
    layout: HomeLayout,
    onMove: (String, Boolean) -> Unit,
    onHidden: (String, Boolean) -> Unit,
    onLayout: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onClose() }
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                ScreenHeader(
                    title = stringResource(R.string.home_customize_title),
                    subtitle = stringResource(R.string.home_customize_subtitle),
                    onClose = onClose,
                )
            }
            item(key = "layout") {
                SettingsGroup(title = stringResource(R.string.home_layout_dashboard)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            shape = CircleShape,
                            selected = layout.layout == Layouts.DASHBOARD,
                            onClick = { onLayout(Layouts.DASHBOARD) },
                            label = { Text(stringResource(R.string.home_layout_dashboard)) },
                        )
                        FilterChip(
                            shape = CircleShape,
                            selected = layout.layout == Layouts.MINIMAL,
                            onClick = { onLayout(Layouts.MINIMAL) },
                            label = { Text(stringResource(R.string.home_layout_minimal)) },
                        )
                    }
                    Text(
                        text = stringResource(
                            if (layout.layout == Layouts.MINIMAL) {
                                R.string.layout_preview_minimal
                            } else {
                                R.string.layout_preview_dashboard
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    )
                }
            }
            item(key = "cards") {
                val order = layout.order.filter { it in HomeCards.defaultOrder } +
                    HomeCards.defaultOrder.filter { it !in layout.order }
                SettingsGroup(title = stringResource(R.string.home_cards_title)) {
                    order.forEachIndexed { index, card ->
                        if (index > 0) GroupDivider()
                        CardRow(
                            card = card,
                            visible = card !in layout.hidden,
                            canMoveUp = index > 0,
                            canMoveDown = index < order.lastIndex,
                            onVisible = { onHidden(card, !it) },
                            onMove = { onMove(card, it) },
                        )
                    }
                }
            }
            item(key = "bottom-space") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CardRow(
    card: String,
    visible: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onVisible: (Boolean) -> Unit,
    onMove: (Boolean) -> Unit,
) {
    val accent = cardAccent(card)
    val colors = accent.colors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.container.copy(alpha = 0.55f),
            modifier = Modifier.size(36.dp),
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = cardIcon(card),
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cardName(card),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    if (visible) R.string.level_visible_short else R.string.level_hidden_short,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onMove(true) }, enabled = canMoveUp) {
            Icon(
                Icons.Rounded.KeyboardArrowUp,
                contentDescription = stringResource(R.string.home_move_up),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onMove(false) }, enabled = canMoveDown) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.home_move_down),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = visible,
            onCheckedChange = onVisible,
            colors = SwitchDefaults.colors(checkedTrackColor = colors.container),
        )
    }
}

private fun cardAccent(card: String): Accent = when (card) {
    HomeCards.TODOS -> Accent.TODOS
    HomeCards.NOTES -> Accent.NOTES
    HomeCards.CALENDAR -> Accent.CALENDAR
    HomeCards.FOCUS -> Accent.FOCUS
    else -> Accent.MOTIVATION
}

private fun cardIcon(card: String): ImageVector = when (card) {
    HomeCards.TODOS -> Icons.Rounded.TaskAlt
    HomeCards.NOTES -> Icons.AutoMirrored.Rounded.StickyNote2
    HomeCards.CALENDAR -> Icons.Rounded.CalendarMonth
    HomeCards.FOCUS -> Icons.Rounded.CenterFocusStrong
    else -> Icons.Rounded.AutoAwesome
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
