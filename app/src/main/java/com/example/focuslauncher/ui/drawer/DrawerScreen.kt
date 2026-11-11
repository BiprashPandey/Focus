package com.example.focuslauncher.ui.drawer

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.EmptyState
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.SearchField
import com.example.focuslauncher.ui.components.pressScale
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import com.example.focuslauncher.ui.theme.colors
import kotlinx.coroutines.launch

private sealed interface DrawerEntry {
    data class Header(val letter: Char, val count: Int) : DrawerEntry
    data class Row(val app: AppInfo) : DrawerEntry
    data class Folder(val count: Int, val expanded: Boolean) : DrawerEntry
    data class Tucked(val app: AppInfo) : DrawerEntry
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    state: AppsUiState,
    onQueryChange: (String) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
    onSetLevel: (AppInfo, AppLevel) -> Unit = { _, _ -> },
    focusHeader: String? = null,
    iconSize: Dp = 40.dp,
    onClose: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var tuckedExpanded by remember { mutableStateOf(false) }
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }
    var scrubLetter by remember { mutableStateOf<Char?>(null) }

    val grouped = remember(state.apps) {
        state.apps
            .groupBy { initialOf(it.label) }
            .toSortedMap()
    }
    val entries = remember(grouped, state.tucked, tuckedExpanded) {
        buildList {
            grouped.forEach { (letter, apps) ->
                add(DrawerEntry.Header(letter, apps.size))
                apps.forEach { add(DrawerEntry.Row(it)) }
            }
            if (state.tucked.isNotEmpty()) {
                add(DrawerEntry.Folder(state.tucked.size, tuckedExpanded))
                if (tuckedExpanded) {
                    state.tucked.forEach { add(DrawerEntry.Tucked(it)) }
                }
            }
        }
    }
    val headerIndexes = remember(entries) {
        entries.mapIndexedNotNull { index, entry ->
            if (entry is DrawerEntry.Header) entry.letter to index else null
        }
    }

    fun jumpTo(letter: Char) {
        val index = headerIndexes.firstOrNull { it.first == letter }?.second ?: return
        scope.launch { gridState.scrollToItem(index) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.9f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.surface.copy(alpha = 0.38f)),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.all_apps),
                subtitle = stringResource(R.string.apps_count, state.apps.size + state.tucked.size),
                onClose = onClose,
            )
            SearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.search_apps),
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 10.dp),
            )
            val focusAccent = Accent.FOCUS
            val banner = focusHeader ?: if (state.inFocus) {
                stringResource(R.string.focus_active_banner)
            } else {
                null
            }
            if (banner != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = focusAccent.colors().container.copy(alpha = 0.75f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = focusAccent.colors().content,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = banner,
                            style = MaterialTheme.typography.labelMedium,
                            color = focusAccent.colors().content,
                        )
                    }
                }
            }
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.apps.isEmpty() && state.tucked.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.SearchOff,
                            title = stringResource(R.string.no_apps_found),
                            body = stringResource(R.string.no_apps_hint),
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.weight(1f)) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(
                                items = entries,
                                key = { drawerKey(it) },
                                span = { entry ->
                                    if (entry is DrawerEntry.Header || entry is DrawerEntry.Folder) {
                                        GridItemSpan(maxLineSpan)
                                    } else {
                                        GridItemSpan(1)
                                    }
                                },
                            ) { entry ->
                                when (entry) {
                                    is DrawerEntry.Header -> SectionHeader(
                                        letter = entry.letter,
                                        count = entry.count,
                                    )
                                    is DrawerEntry.Row -> AppCell(
                                        app = entry.app,
                                        state = state,
                                        loadIcon = loadIcon,
                                        iconSize = iconSize,
                                        onClick = { onLaunch(entry.app) },
                                        onLongClick = { menuApp = entry.app },
                                    )
                                    is DrawerEntry.Folder -> TuckedHeader(
                                        count = entry.count,
                                        expanded = entry.expanded,
                                        onToggle = { tuckedExpanded = !tuckedExpanded },
                                    )
                                    is DrawerEntry.Tucked -> AppCell(
                                        app = entry.app,
                                        state = state,
                                        loadIcon = loadIcon,
                                        iconSize = iconSize,
                                        onClick = { onLaunch(entry.app) },
                                        onLongClick = { menuApp = entry.app },
                                    )
                                }
                            }
                        }
                        if (headerIndexes.isNotEmpty() && state.query.isBlank()) {
                            FastScroller(
                                letters = headerIndexes.map { it.first },
                                onJump = ::jumpTo,
                                onScrub = { scrubLetter = it },
                                modifier = Modifier.align(Alignment.CenterEnd),
                            )
                        }
                        scrubLetter?.let { letter ->
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(end = 44.dp),
                                shape = CircleShape,
                                color = scheme.primary,
                            ) {
                                Text(
                                    text = letter.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val selectedApp = menuApp
    if (selectedApp != null) {
        ModalBottomSheet(
            onDismissRequest = { menuApp = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = scheme.surfaceContainerLow,
        ) {
            AppActionsSheet(
                app = selectedApp,
                isProtected = selectedApp.packageName in state.protectedPackages,
                loadIcon = loadIcon,
                onSetLevel = { app, level ->
                    menuApp = null
                    onSetLevel(app, level)
                },
                onDismiss = { menuApp = null },
            )
        }
    }
}

private fun drawerKey(entry: DrawerEntry): String = when (entry) {
    is DrawerEntry.Header -> "header-${entry.letter}"
    is DrawerEntry.Row -> entry.app.key
    is DrawerEntry.Folder -> "folder"
    is DrawerEntry.Tucked -> "tucked-${entry.app.key}"
}

private fun initialOf(label: String): Char {
    val first = label.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()
    return if (first != null && first.isLetter()) first else '#'
}

@Composable
private fun SectionHeader(letter: Char, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppInfo,
    state: AppsUiState,
    loadIcon: suspend (AppInfo) -> Drawable?,
    iconSize: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val interaction = remember(app.key) { MutableInteractionSource() }
    val gated = app.packageName in state.gatedPackages
    val gatedColors = Accent.TODOS.colors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .pressScale(interaction)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            AppIcon(app = app, loadIcon = loadIcon, size = iconSize)
            if (gated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(gatedColors.container),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = gatedColors.content,
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TuckedHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
            .combinedClickable(role = Role.Button, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.tucked_folder, count),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FastScroller(
    letters: List<Char>,
    onJump: (Char) -> Unit,
    onScrub: (Char?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(30.dp)
            .padding(vertical = 12.dp)
            .pointerInput(letters) {
                detectTapGestures { offset ->
                    onJump(letterAt(offset.y, size.height, letters))
                }
            }
            .pointerInput(letters) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        onScrub(letterAt(offset.y, size.height, letters))
                    },
                    onDragCancel = { onScrub(null) },
                    onDragEnd = { onScrub(null) },
                ) { change, _ ->
                    change.consume()
                    val letter = letterAt(change.position.y, size.height, letters)
                    onJump(letter)
                    onScrub(letter)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        for (letter in letters) {
            Text(
                text = letter.toString(),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun letterAt(y: Float, height: Int, letters: List<Char>): Char {
    val index = ((y / height.coerceAtLeast(1)) * letters.size).toInt()
        .coerceIn(0, letters.size - 1)
    return letters[index]
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppActionsSheet(
    app: AppInfo,
    isProtected: Boolean,
    loadIcon: suspend (AppInfo) -> Drawable?,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(app = app, loadIcon = loadIcon, size = 56.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = app.label, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        )
        SheetAction(
            icon = Icons.Rounded.Info,
            label = stringResource(R.string.app_info),
            onClick = {
                onDismiss()
                context.startActivity(appInfoIntent(app))
            },
        )
        SheetAction(
            icon = Icons.Rounded.Delete,
            label = stringResource(R.string.uninstall),
            onClick = {
                onDismiss()
                context.startActivity(uninstallIntent(app))
            },
        )
        if (isProtected) {
            SheetAction(
                icon = Icons.Rounded.Lock,
                label = stringResource(R.string.protected_app),
                enabled = false,
                onClick = {},
            )
        } else {
            SheetAction(
                icon = Icons.Rounded.Archive,
                label = stringResource(R.string.hide_app),
                onClick = { onSetLevel(app, AppLevel.TUCKED) },
            )
            SheetAction(
                icon = Icons.Rounded.VisibilityOff,
                label = stringResource(R.string.hide_app_full),
                onClick = { onSetLevel(app, AppLevel.HIDDEN) },
            )
            SheetAction(
                icon = Icons.Rounded.Visibility,
                label = stringResource(R.string.unhide_app),
                onClick = { onSetLevel(app, AppLevel.VISIBLE) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            },
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
        )
    }
}

private fun appInfoIntent(app: AppInfo): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${app.packageName}")
    }

private fun uninstallIntent(app: AppInfo): Intent =
    Intent(Intent.ACTION_DELETE).apply {
        data = Uri.parse("package:${app.packageName}")
    }

@Preview(showBackground = true)
@Composable
private fun DrawerScreenPreview() {
    val sample = listOf(
        AppInfo("Camera", "com.android.camera", "com.android.camera/.Main", "0"),
        AppInfo("Clock", "com.android.deskclock", "com.android.deskclock/.Main", "0"),
    )
    FocusLauncherTheme {
        DrawerScreen(
            state = AppsUiState(apps = sample),
            onQueryChange = {},
            onLaunch = {},
            loadIcon = { null },
        )
    }
}
