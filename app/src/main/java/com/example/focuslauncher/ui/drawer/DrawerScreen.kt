package com.example.focuslauncher.ui.drawer

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import kotlinx.coroutines.launch

private sealed interface DrawerItem {
    data class Header(val letter: Char) : DrawerItem
    data class Row(val app: AppInfo) : DrawerItem
}

@Composable
fun DrawerScreen(
    state: AppsUiState,
    onQueryChange: (String) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
    onSetLevel: (AppInfo, AppLevel) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val items = remember(state.apps) {
        state.apps
            .groupBy { initialOf(it.label) }
            .toSortedMap()
            .flatMap { (letter, apps) ->
                listOf(DrawerItem.Header(letter)) + apps.map { DrawerItem.Row(it) }
            }
    }
    val headers = remember(items) {
        items.mapIndexedNotNull { index, item ->
            if (item is DrawerItem.Header) item.letter to index else null
        }
    }

    fun jumpTo(letter: Char) {
        val index = headers.firstOrNull { it.first == letter }?.second ?: return
        scope.launch { listState.scrollToItem(index) }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
            if (state.inFocus) {
                Text(
                    text = stringResource(R.string.focus_active_banner),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            if (state.apps.isEmpty() && !state.isLoading) {
                Text(
                    text = stringResource(R.string.no_apps_found),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { drawerKey(it) }) { item ->
                        when (item) {
                            is DrawerItem.Header -> SectionHeader(letter = item.letter)
                            is DrawerItem.Row -> DrawerRow(
                                app = item.app,
                                isProtected = item.app.packageName in state.protectedPackages,
                                onLaunch = onLaunch,
                                onSetLevel = onSetLevel,
                                loadIcon = loadIcon,
                            )
                        }
                    }
                    if (state.tucked.isNotEmpty()) {
                        item(key = "tucked-folder") {
                            TuckedFolder(
                                tucked = state.tucked,
                                protectedPackages = state.protectedPackages,
                                onLaunch = onLaunch,
                                onSetLevel = onSetLevel,
                                loadIcon = loadIcon,
                            )
                        }
                    }
                }
                if (headers.isNotEmpty()) {
                    FastScroller(
                        letters = headers.map { it.first },
                        onJump = ::jumpTo,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}

private fun drawerKey(item: DrawerItem): String = when (item) {
    is DrawerItem.Header -> "header-${item.letter}"
    is DrawerItem.Row -> item.app.key
}

private fun initialOf(label: String): Char {
    val first = label.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()
    return if (first != null && first.isLetter()) first else '#'
}

@Composable
private fun SectionHeader(letter: Char) {
    Text(
        text = letter.toString(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerRow(
    app: AppInfo,
    isProtected: Boolean,
    onLaunch: (AppInfo) -> Unit,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    val context = LocalContext.current
    var menuOpen by remember(app.key) { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onLaunch(app) },
                    onLongClick = { menuOpen = true },
                )
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(app = app, loadIcon = loadIcon)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.app_info)) },
                onClick = {
                    menuOpen = false
                    context.startActivity(appInfoIntent(app))
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.uninstall)) },
                onClick = {
                    menuOpen = false
                    context.startActivity(uninstallIntent(app))
                },
            )
            if (isProtected) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.protected_app)) },
                    enabled = false,
                    onClick = { menuOpen = false },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.hide_app)) },
                    onClick = {
                        menuOpen = false
                        onSetLevel(app, AppLevel.TUCKED)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.hide_app_full)) },
                    onClick = {
                        menuOpen = false
                        onSetLevel(app, AppLevel.HIDDEN)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.unhide_app)) },
                    onClick = {
                        menuOpen = false
                        onSetLevel(app, AppLevel.VISIBLE)
                    },
                )
            }
        }
    }
    HorizontalDivider()
}

private fun appInfoIntent(app: AppInfo): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${app.packageName}")
    }

private fun uninstallIntent(app: AppInfo): Intent =
    Intent(Intent.ACTION_DELETE).apply {
        data = Uri.parse("package:${app.packageName}")
    }

@Composable
private fun FastScroller(
    letters: List<Char>,
    onJump: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .pointerInput(letters) {
                detectTapGestures { offset ->
                    onJump(letterAt(offset.y, size.height, letters))
                }
            }
            .pointerInput(letters) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    onJump(letterAt(change.position.y, size.height, letters))
                }
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
private fun TuckedFolder(
    tucked: List<AppInfo>,
    protectedPackages: Set<String>,
    onLaunch: (AppInfo) -> Unit,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { expanded = !expanded })
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.tucked_folder, tucked.size),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (expanded) "−" else "+",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (expanded) {
            for (app in tucked) {
                DrawerRow(
                    app = app,
                    isProtected = app.packageName in protectedPackages,
                    onLaunch = onLaunch,
                    onSetLevel = onSetLevel,
                    loadIcon = loadIcon,
                )
            }
        }
    }
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
