package com.example.focuslauncher.ui.home

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.rules.AppListRules
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.components.SearchField
import com.example.focuslauncher.ui.components.pressScale
import com.example.focuslauncher.ui.drawer.AppIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DockSection(
    dock: List<AppInfo?>,
    allApps: List<AppInfo>,
    onLaunch: (AppInfo) -> Unit,
    onPick: (index: Int, app: AppInfo?) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 52.dp,
) {
    var pickerFor by remember { mutableStateOf<Int?>(null) }

    GlassCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dock.forEachIndexed { index, app ->
                val slotLabel = stringResource(R.string.dock_slot, index + 1)
                val interaction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(iconSize + 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .pressScale(interaction)
                        .combinedClickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = {
                                if (app == null) pickerFor = index else onLaunch(app)
                            },
                            onLongClick = { pickerFor = index },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (app != null) {
                        AppIcon(app = app, loadIcon = loadIcon, size = iconSize)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(iconSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = slotLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    val target = pickerFor
    if (target != null) {
        val takenKeys = dock
            .filterIndexed { index, _ -> index != target }
            .mapNotNull { it?.key }
            .toSet()
        DockPickerDialog(
            current = dock.getOrNull(target),
            apps = allApps.filterNot { it.key in takenKeys },
            loadIcon = loadIcon,
            onPick = { app ->
                onPick(target, app)
                pickerFor = null
            },
            onRemove = {
                onPick(target, null)
                pickerFor = null
            },
            onDismiss = { pickerFor = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockPickerDialog(
    current: AppInfo?,
    apps: List<AppInfo>,
    loadIcon: suspend (AppInfo) -> Drawable?,
    onPick: (AppInfo) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) { AppListRules.filter(apps, query) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.choose_app),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (current != null) {
                    TextButton(onClick = onRemove) {
                        Text(stringResource(R.string.remove_from_dock))
                    }
                } else {
                    Spacer(modifier = Modifier.size(8.dp))
                }
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.search_apps),
                )
                Spacer(modifier = Modifier.size(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.key }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    role = Role.Button,
                                    onClick = { onPick(app) },
                                )
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppIcon(app = app, loadIcon = loadIcon, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
