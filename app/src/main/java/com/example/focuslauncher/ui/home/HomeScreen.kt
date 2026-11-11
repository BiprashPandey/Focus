package com.example.focuslauncher.ui.home

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.focuslauncher.R
import com.example.focuslauncher.data.settings.HomeCards
import com.example.focuslauncher.data.settings.HomeLayout
import com.example.focuslauncher.data.settings.Layouts
import com.example.focuslauncher.data.theme.ThemeConfig
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.calendar.CalendarCard
import com.example.focuslauncher.ui.calendar.CalendarUiState
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.drawer.AppsUiState
import com.example.focuslauncher.ui.drawer.DrawerScreen
import com.example.focuslauncher.ui.focus.ActiveFocusBar
import com.example.focuslauncher.ui.focus.FocusCard
import com.example.focuslauncher.ui.focus.FocusSettingsScreen
import com.example.focuslauncher.ui.focus.FocusSettingsUiState
import com.example.focuslauncher.ui.focus.FocusUiState
import com.example.focuslauncher.ui.manage.ManageAppsScreen
import com.example.focuslauncher.ui.manage.ManageAppsUiState
import com.example.focuslauncher.ui.motivation.MotivationCard
import com.example.focuslauncher.ui.motivation.MotivationUiState
import com.example.focuslauncher.ui.notes.NotesCard
import com.example.focuslauncher.ui.notes.NotesScreen
import com.example.focuslauncher.ui.notes.NotesUiState
import com.example.focuslauncher.ui.pause.PauseScreen
import com.example.focuslauncher.ui.pause.rememberPauseLine
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.ThemeSettingsScreen
import com.example.focuslauncher.ui.theme.colors
import com.example.focuslauncher.ui.todos.TodosCard
import com.example.focuslauncher.ui.todos.TodosScreen
import com.example.focuslauncher.ui.todos.TodosUiState
import com.example.focuslauncher.ui.usage.UsagePermissionScreen
import com.example.focuslauncher.data.notes.Note
import com.example.focuslauncher.data.todos.Todo
import com.example.focuslauncher.domain.focus.FocusSchedule
import com.example.focuslauncher.domain.focus.countdown

private enum class Overlay {
    MANAGE,
    USAGE,
    FOCUS,
    TODOS,
    NOTES,
    HOME,
    THEME,
}

@Composable
fun HomeScreen(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
    appsState: AppsUiState,
    homeState: HomeUiState,
    manageState: ManageAppsUiState,
    focusState: FocusUiState,
    focusSettingsState: FocusSettingsUiState,
    todosState: TodosUiState,
    notesState: NotesUiState,
    calendarState: CalendarUiState,
    motivationState: MotivationUiState,
    homeLayout: HomeLayout,
    themeConfig: ThemeConfig,
    iconSize: Dp,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onSetLevel: (AppInfo, AppLevel) -> Unit,
    onManageQuery: (String) -> Unit,
    onManageSetLevel: (AppInfo, AppLevel) -> Unit,
    onManageSetLimit: (AppInfo, Int?) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onRefreshUsage: () -> Unit,
    onStartFocus: (Long, Int) -> Unit,
    onStopFocus: (String?) -> Unit,
    onFocusSettingsQuery: (String) -> Unit,
    onSelectFocusProfile: (Long) -> Unit,
    onToggleFocusAllowed: (AppInfo, Boolean) -> Unit,
    onRefreshFocusPermissions: () -> Unit,
    onFocusDndToggle: (Boolean) -> Unit,
    onOpenDndSettings: () -> Unit,
    onSaveFocusSchedule: (FocusSchedule) -> Unit,
    onClearFocusSchedule: () -> Unit,
    onTodoAdd: (String) -> Unit,
    onTodoToggle: (Todo) -> Unit,
    onTodoDelete: (Long) -> Unit,
    onTodosClearCompleted: () -> Unit,
    onNoteQuickAdd: (String) -> Unit,
    onNoteTogglePin: (Note) -> Unit,
    onNoteDelete: (Long) -> Unit,
    onCalendarConnect: () -> Unit,
    onIntentionSave: (String) -> Unit,
    onIntentionToggle: () -> Unit,
    onToneChange: (String) -> Unit,
    onCardMove: (String, Boolean) -> Unit,
    onCardHidden: (String, Boolean) -> Unit,
    onLayoutChange: (String) -> Unit,
    onThemeMode: (String) -> Unit,
    onThemeSource: (String) -> Unit,
    onThemePreset: (com.example.focuslauncher.data.theme.ThemePreset) -> Unit,
    onThemeSeedHex: (Long) -> Unit,
    onThemeCorner: (String) -> Unit,
    onThemeIconSize: (Int) -> Unit,
    onThemeReset: () -> Unit,
    onDockPick: (Int, AppInfo?) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var pendingPause by remember { mutableStateOf<AppInfo?>(null) }
    BackHandler(enabled = drawerOpen) { drawerOpen = false }
    BackHandler(enabled = overlay != null) { overlay = null }

    fun handleLaunch(app: AppInfo) {
        if (app.packageName in appsState.gatedPackages ||
            app.packageName in appsState.blockedPackages
        ) {
            pendingPause = app
        } else {
            onLaunchApp(app)
        }
    }

    val minimal = homeLayout.layout == Layouts.MINIMAL
    val focusActive = focusState.active != null
    val cards = if (minimal) {
        if (focusActive) listOf(HomeCards.FOCUS) else emptyList()
    } else {
        homeLayout.visibleCards().let { visible ->
            if (focusActive && HomeCards.FOCUS !in visible) visible + HomeCards.FOCUS else visible
        }
    }
    val focusHeader = focusState.active?.let { active ->
        val name = focusState.profiles.firstOrNull { it.id == active.profileId }?.name
        "$name · ${active.countdown(focusState.now)}"
    }
    LaunchedEffect(overlay) {
        if (overlay == Overlay.FOCUS) onRefreshFocusPermissions()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackdrop()
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                HomeHeader(
                    isRoleAvailable = isRoleAvailable,
                    isDefaultLauncher = isDefaultLauncher,
                    onRequestDefault = onRequestDefault,
                    onManageClick = { overlay = Overlay.MANAGE },
                    onUsageClick = {
                        onRefreshUsage()
                        overlay = Overlay.USAGE
                    },
                    onFocusClick = { overlay = Overlay.FOCUS },
                    onHomeClick = { overlay = Overlay.HOME },
                    onThemeClick = { overlay = Overlay.THEME },
                    modifier = Modifier.onSwipeUp { drawerOpen = true },
                )
                if (minimal) {
                    MinimalIntention(
                        intention = motivationState.intention,
                        hasIntention = motivationState.hasIntention,
                    )
                }
                if (cards.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(18.dp))
                    HomeCardPages(
                        cards = cards,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onSwipeUp = { drawerOpen = true },
                    ) { card ->
                        when (card) {
                            HomeCards.MOTIVATION -> MotivationCard(
                                state = motivationState,
                                onSaveIntention = onIntentionSave,
                                onToggleIntentionDone = onIntentionToggle,
                                onToneChange = onToneChange,
                            )
                            HomeCards.TODOS -> TodosCard(
                                state = todosState,
                                onAdd = onTodoAdd,
                                onToggle = onTodoToggle,
                                onOpenList = { overlay = Overlay.TODOS },
                            )
                            HomeCards.FOCUS -> FocusCard(
                                state = focusState,
                                onStart = onStartFocus,
                                onStop = onStopFocus,
                            )
                            HomeCards.NOTES -> NotesCard(
                                state = notesState,
                                onQuickAdd = onNoteQuickAdd,
                                onOpenNotes = { overlay = Overlay.NOTES },
                            )
                            HomeCards.CALENDAR -> CalendarCard(
                                state = calendarState,
                                onConnect = onCalendarConnect,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            AllAppsPill(onClick = { drawerOpen = true })
            val running = focusState.active
            if (running != null) {
                ActiveFocusBar(
                    active = running,
                    now = focusState.now,
                    profileName = focusState.profiles
                        .firstOrNull { it.id == running.profileId }?.name,
                    onStop = onStopFocus,
                )
            }
            DockSection(
                dock = homeState.dock,
                allApps = homeState.allApps,
                onLaunch = ::handleLaunch,
                onPick = onDockPick,
                loadIcon = loadIcon,
                iconSize = iconSize,
                modifier = Modifier.navigationBarsPadding().padding(bottom = 6.dp),
            )
        }

        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 8 },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 10 },
            modifier = Modifier.zIndex(1f),
        ) {
            DrawerScreen(
                state = appsState,
                onQueryChange = onQueryChange,
                onLaunch = ::handleLaunch,
                loadIcon = loadIcon,
                onSetLevel = onSetLevel,
                focusHeader = focusHeader,
                iconSize = iconSize,
                onClose = { drawerOpen = false },
                modifier = Modifier.onSwipeDown { drawerOpen = false },
            )
        }

        AnimatedVisibility(
            visible = overlay == Overlay.MANAGE,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            ManageAppsScreen(
                state = manageState,
                onQueryChange = onManageQuery,
                onSetLevel = onManageSetLevel,
                onSetLimit = onManageSetLimit,
                onClose = { overlay = null },
                loadIcon = loadIcon,
                iconSize = iconSize,
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.USAGE,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            UsagePermissionScreen(
                hasPermission = appsState.hasUsagePermission,
                onOpenSettings = onOpenUsageSettings,
                onRefresh = onRefreshUsage,
                onClose = { overlay = null },
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.FOCUS,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            FocusSettingsScreen(
                state = focusSettingsState,
                focusState = focusState,
                onStartFocus = onStartFocus,
                onStopFocus = onStopFocus,
                onQueryChange = onFocusSettingsQuery,
                onSelectProfile = onSelectFocusProfile,
                onToggleAllowed = onToggleFocusAllowed,
                onDndToggle = onFocusDndToggle,
                onOpenDndSettings = onOpenDndSettings,
                onSaveSchedule = onSaveFocusSchedule,
                onClearSchedule = onClearFocusSchedule,
                onClose = { overlay = null },
                loadIcon = loadIcon,
                iconSize = iconSize,
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.TODOS,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            TodosScreen(
                state = todosState,
                onAdd = onTodoAdd,
                onToggle = onTodoToggle,
                onDelete = onTodoDelete,
                onClearCompleted = onTodosClearCompleted,
                onClose = { overlay = null },
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.NOTES,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            NotesScreen(
                state = notesState,
                onTogglePin = onNoteTogglePin,
                onDelete = onNoteDelete,
                onClose = { overlay = null },
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.HOME,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            HomeCustomizationScreen(
                layout = homeLayout,
                onMove = onCardMove,
                onHidden = onCardHidden,
                onLayout = onLayoutChange,
                onClose = { overlay = null },
            )
        }
        AnimatedVisibility(
            visible = overlay == Overlay.THEME,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.zIndex(2f),
        ) {
            ThemeSettingsScreen(
                config = themeConfig,
                onMode = onThemeMode,
                onSource = onThemeSource,
                onPreset = onThemePreset,
                onSeedHex = onThemeSeedHex,
                onCorner = onThemeCorner,
                onIconSize = onThemeIconSize,
                onReset = onThemeReset,
                onClose = { overlay = null },
            )
        }
        pendingPause?.let { gated ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(200)),
                modifier = Modifier.zIndex(3f),
            ) {
                PauseScreen(
                    app = gated,
                    todayMinutes = appsState.usageMinutes[gated.packageName] ?: 0,
                    motivationalLine = rememberPauseLine(),
                    onOpenAnyway = {
                        pendingPause = null
                        onLaunchApp(it)
                    },
                    onDoSomethingElse = {
                        pendingPause = null
                        drawerOpen = false
                    },
                    onClose = { pendingPause = null },
                    loadIcon = loadIcon,
                )
            }
        }
    }
}

@Composable
private fun AllAppsPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f),
            ),
            modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = stringResource(R.string.all_apps),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun MinimalIntention(
    intention: String,
    hasIntention: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.MOTIVATION
    val colors = accent.colors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.bright,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.intention_title),
                style = MaterialTheme.typography.labelLarge,
                color = colors.content,
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = if (hasIntention) intention else stringResource(R.string.intention_hint),
            style = MaterialTheme.typography.headlineSmall,
            color = if (hasIntention) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private fun Modifier.onSwipeUp(onSwipe: () -> Unit): Modifier = pointerInput(Unit) {
    var total = 0f
    detectVerticalDragGestures(
        onDragStart = { total = 0f },
        onDragCancel = { total = 0f },
        onDragEnd = { if (total < -SWIPE_PX) onSwipe() },
        onVerticalDrag = { _, dragAmount -> total += dragAmount },
    )
}

private fun Modifier.onSwipeDown(onSwipe: () -> Unit): Modifier = pointerInput(Unit) {
    var total = 0f
    detectVerticalDragGestures(
        onDragStart = { total = 0f },
        onDragCancel = { total = 0f },
        onDragEnd = { if (total > SWIPE_PX) onSwipe() },
        onVerticalDrag = { _, dragAmount -> total += dragAmount },
    )
}

private const val SWIPE_PX = 120f
