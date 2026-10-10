package com.example.focuslauncher.ui.home

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.rules.AppListRules
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
) {
    var pickerFor by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dock.forEachIndexed { index, app ->
            val slotLabel = stringResource(R.string.dock_slot, index + 1)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .combinedClickable(
                        onClick = {
                            if (app == null) pickerFor = index else onLaunch(app)
                        },
                        onLongClick = { pickerFor = index },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (app != null) {
                    AppIcon(app = app, loadIcon = loadIcon, modifier = Modifier.size(52.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = slotLabel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    val target = pickerFor
    if (target != null) {
        DockPickerDialog(
            current = dock.getOrNull(target),
            apps = allApps,
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
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.choose_app),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (current != null) {
                    TextButton(onClick = onRemove) {
                        Text(stringResource(R.string.remove_from_dock))
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_apps)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.key }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = { onPick(app) })
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppIcon(app = app, loadIcon = loadIcon)
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
