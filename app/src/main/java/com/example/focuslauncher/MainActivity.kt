package com.example.focuslauncher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.focuslauncher.data.settings.HomeCards
import com.example.focuslauncher.data.settings.Layouts
import com.example.focuslauncher.domain.focus.countdown
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.calendar.CalendarCard
import com.example.focuslauncher.ui.drawer.AppsUiState
import com.example.focuslauncher.ui.drawer.AppsViewModel
import com.example.focuslauncher.ui.drawer.DrawerScreen
import com.example.focuslauncher.ui.focus.FocusCard
import com.example.focuslauncher.ui.focus.ActiveFocusBar
import com.example.focuslauncher.ui.focus.FocusSettingsScreen
import com.example.focuslauncher.ui.focus.FocusSettingsViewModel
import com.example.focuslauncher.ui.focus.FocusViewModel
import com.example.focuslauncher.ui.home.DockSection
import com.example.focuslauncher.ui.home.HomeCustomizationScreen
import com.example.focuslauncher.ui.home.HomeUiState
import com.example.focuslauncher.ui.home.HomeViewModel
import com.example.focuslauncher.ui.home.HomeSettingsViewModel
import com.example.focuslauncher.ui.manage.ManageAppsScreen
import com.example.focuslauncher.ui.manage.ManageAppsViewModel
import com.example.focuslauncher.ui.motivation.MotivationCard
import com.example.focuslauncher.ui.motivation.MotivationViewModel
import com.example.focuslauncher.ui.notes.NotesCard
import com.example.focuslauncher.ui.notes.NotesScreen
import com.example.focuslauncher.ui.notes.NotesViewModel
import com.example.focuslauncher.ui.pause.PauseScreen
import com.example.focuslauncher.ui.pause.rememberPauseLine
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import com.example.focuslauncher.ui.theme.ThemeViewModel
import com.example.focuslauncher.ui.todos.TodosCard
import com.example.focuslauncher.ui.todos.TodosScreen
import com.example.focuslauncher.ui.todos.TodosViewModel
import com.example.focuslauncher.ui.calendar.CalendarViewModel
import com.example.focuslauncher.ui.usage.UsagePermissionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appsViewModel: AppsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val manageViewModel: ManageAppsViewModel by viewModels()
    private val focusViewModel: FocusViewModel by viewModels()
    private val focusSettingsViewModel: FocusSettingsViewModel by viewModels()
    private val todosViewModel: TodosViewModel by viewModels()
    private val notesViewModel: NotesViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val motivationViewModel: MotivationViewModel by viewModels()
    private val homeSettingsViewModel: HomeSettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        setContent {
            val themeConfig by themeViewModel.config.collectAsState()
            FocusLauncherTheme(config = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // On home: back press does nothing (drawer handles its own back).
                    BackHandler(enabled = true) { /* stay on home */ }
                    val appsState by appsViewModel.uiState.collectAsState()
                    val homeState by homeViewModel.uiState.collectAsState()
                    val manageState by manageViewModel.uiState.collectAsState()
                    val focusState by focusViewModel.uiState.collectAsState()
                    val focusSettingsState by focusSettingsViewModel.uiState.collectAsState()
                    val todosState by todosViewModel.uiState.collectAsState()
                    val notesState by notesViewModel.uiState.collectAsState()
                    val calendarState by calendarViewModel.uiState.collectAsState()
                    val motivationState by motivationViewModel.uiState.collectAsState()
                    val homeLayout by homeSettingsViewModel.layout.collectAsState()
                    HomeScreen(
                        isRoleAvailable = isHomeRoleAvailable(),
                        isDefaultLauncher = isDefaultLauncher(),
                        onRequestDefault = ::requestHomeRole,
                        appsState = appsState,
                        homeState = homeState,
                        manageState = manageState,
                        focusState = focusState,
                        focusSettingsState = focusSettingsState,
                        todosState = todosState,
                        notesState = notesState,
                        calendarState = calendarState,
                        motivationState = motivationState,
                        homeLayout = homeLayout,
                        iconSize = themeConfig.iconSizeDp.dp,
                        onQueryChange = appsViewModel::onQueryChange,
                        onLaunchApp = ::launchApp,
                        onSetLevel = appsViewModel::setLevel,
                        onManageQuery = manageViewModel::onQueryChange,
                        onManageSetLevel = manageViewModel::setLevel,
                        onManageSetLimit = manageViewModel::setDailyLimit,
                        onOpenUsageSettings = ::openUsageSettings,
                        onRefreshUsage = appsViewModel::refreshUsage,
                        onStartFocus = ::startFocus,
                        onStopFocus = focusViewModel::stop,
                        onFocusSettingsQuery = focusSettingsViewModel::onQueryChange,
                        onSelectFocusProfile = focusSettingsViewModel::onSelectProfile,
                        onToggleFocusAllowed = focusSettingsViewModel::onToggleAllowed,
                        onFocusDndToggle = ::onFocusDndToggle,
                        onOpenDndSettings = ::openDndSettings,
                        onSaveFocusSchedule = focusSettingsViewModel::onSaveSchedule,
                        onClearFocusSchedule = focusSettingsViewModel::onClearSchedule,
                        onTodoAdd = todosViewModel::add,
                        onTodoToggle = todosViewModel::toggle,
                        onTodoDelete = todosViewModel::delete,
                        onTodosClearCompleted = todosViewModel::clearCompleted,
                        onNoteQuickAdd = notesViewModel::quickAdd,
                        onNoteTogglePin = notesViewModel::togglePin,
                        onNoteDelete = notesViewModel::delete,
                        onCalendarConnect = ::requestCalendarPermission,
                        onIntentionSave = motivationViewModel::saveIntention,
                        onIntentionToggle = motivationViewModel::toggleIntentionDone,
                        onToneChange = motivationViewModel::setTone,
                        onCardMove = homeSettingsViewModel::move,
                        onCardHidden = homeSettingsViewModel::setHidden,
                        onLayoutChange = homeSettingsViewModel::setLayout,
                        onDockPick = homeViewModel::setDockSlot,
                        loadIcon = appsViewModel::loadIcon,
                    )
                }
            }
        }
    }

    private fun launchApp(app: AppInfo) {
        if (!appsViewModel.launch(app)) {
            Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /** Start a focus session; notification permission is best-effort. */
    private fun startFocus(profileId: Long, durationMin: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        focusViewModel.start(profileId, durationMin)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Best-effort: the session runs with or without notifications.
        }

    private fun requestCalendarPermission() {
        calendarPermissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
    }

    private val calendarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Granted or not, refresh so the card shows events or the empty state.
            calendarViewModel.refresh()
        }

    private fun onFocusDndToggle(enabled: Boolean) {
        if (enabled && !hasDndAccess()) {
            openDndSettings()
            return
        }
        focusSettingsViewModel.onDndToggle(enabled)
    }

    private fun hasDndAccess(): Boolean =
        getSystemService(android.app.NotificationManager::class.java)
            ?.isNotificationPolicyAccessGranted == true

    private fun openDndSettings() {
        try {
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUsageSettings() {
        try {
            startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        appsViewModel.refreshUsage()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // singleTask relaunch (Home press): return to top. Single screen for now,
        // so just update the intent; later phases reset nav to home destination here.
        setIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun roleManager(): RoleManager = getSystemService(RoleManager::class.java)

    private fun isHomeRoleAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return roleManager().isRoleAvailable(RoleManager.ROLE_HOME)
    }

    private fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return roleManager().isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun requestHomeRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val rm = roleManager()
        if (!rm.isRoleAvailable(RoleManager.ROLE_HOME) || rm.isRoleHeld(RoleManager.ROLE_HOME)) return
        homeRoleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
    }

    private val homeRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Recompose with fresh role state after the system dialog closes.
            render()
        }
}

@Composable
fun HomeScreen(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
    appsState: AppsUiState,
    homeState: HomeUiState,
    manageState: com.example.focuslauncher.ui.manage.ManageAppsUiState,
    focusState: com.example.focuslauncher.ui.focus.FocusUiState,
    focusSettingsState: com.example.focuslauncher.ui.focus.FocusSettingsUiState,
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
    onFocusDndToggle: (Boolean) -> Unit,
    onOpenDndSettings: () -> Unit,
    onSaveFocusSchedule: (com.example.focuslauncher.domain.focus.FocusSchedule) -> Unit,
    onClearFocusSchedule: () -> Unit,
    todosState: com.example.focuslauncher.ui.todos.TodosUiState,
    notesState: com.example.focuslauncher.ui.notes.NotesUiState,
    calendarState: com.example.focuslauncher.ui.calendar.CalendarUiState,
    motivationState: com.example.focuslauncher.ui.motivation.MotivationUiState,
    homeLayout: com.example.focuslauncher.data.settings.HomeLayout,
    iconSize: Dp,
    onTodoAdd: (String) -> Unit,
    onTodoToggle: (com.example.focuslauncher.data.todos.Todo) -> Unit,
    onTodoDelete: (Long) -> Unit,
    onTodosClearCompleted: () -> Unit,
    onNoteQuickAdd: (String) -> Unit,
    onNoteTogglePin: (com.example.focuslauncher.data.notes.Note) -> Unit,
    onNoteDelete: (Long) -> Unit,
    onCalendarConnect: () -> Unit,
    onIntentionSave: (String) -> Unit,
    onIntentionToggle: () -> Unit,
    onToneChange: (String) -> Unit,
    onCardMove: (String, Boolean) -> Unit,
    onCardHidden: (String, Boolean) -> Unit,
    onLayoutChange: (String) -> Unit,
    onDockPick: (Int, AppInfo?) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    var manageOpen by remember { mutableStateOf(false) }
    var usageOpen by remember { mutableStateOf(false) }
    var focusSettingsOpen by remember { mutableStateOf(false) }
    var todosOpen by remember { mutableStateOf(false) }
    var notesOpen by remember { mutableStateOf(false) }
    var homeCustomOpen by remember { mutableStateOf(false) }
    var pendingPause by remember { mutableStateOf<AppInfo?>(null) }
    BackHandler(enabled = drawerOpen) { drawerOpen = false }
    BackHandler(enabled = manageOpen) { manageOpen = false }
    BackHandler(enabled = usageOpen) { usageOpen = false }
    BackHandler(enabled = focusSettingsOpen) { focusSettingsOpen = false }
    BackHandler(enabled = todosOpen) { todosOpen = false }
    BackHandler(enabled = notesOpen) { notesOpen = false }
    BackHandler(enabled = homeCustomOpen) { homeCustomOpen = false }
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
        // Minimal mode: calm text home. The running timer always stays visible.
        if (focusActive) listOf(HomeCards.FOCUS) else emptyList()
    } else {
        homeLayout.visibleCards().let { visible ->
            if (focusActive && HomeCards.FOCUS !in visible) visible + HomeCards.FOCUS else visible
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .onSwipeUp { drawerOpen = true },
            ) {
                BlankHome(
                    isRoleAvailable = isRoleAvailable,
                    isDefaultLauncher = isDefaultLauncher,
                    onRequestDefault = onRequestDefault,
                    onManageClick = { manageOpen = true },
                    onUsageClick = {
                        onRefreshUsage()
                        usageOpen = true
                    },
                    onFocusClick = { focusSettingsOpen = true },
                    onHomeClick = { homeCustomOpen = true },
                )
                HorizontalDivider()
                if (minimal && !focusActive) {
                    MinimalIntention(
                        intention = motivationState.intention,
                        hasIntention = motivationState.hasIntention,
                    )
                }
                for (card in cards) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                            onOpenList = { todosOpen = true },
                        )
                        HomeCards.FOCUS -> FocusCard(
                            state = focusState,
                            onStart = onStartFocus,
                            onStop = onStopFocus,
                        )
                        HomeCards.NOTES -> NotesCard(
                            state = notesState,
                            onQuickAdd = onNoteQuickAdd,
                            onOpenNotes = { notesOpen = true },
                        )
                        HomeCards.CALENDAR -> CalendarCard(
                            state = calendarState,
                            onConnect = onCalendarConnect,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.swipe_up_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clickable { drawerOpen = true }
                        .padding(8.dp),
                )
            }
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
            )
        }
        // Explicit z-order: drawer < sheets < pause. Dialogs (dock picker)
        // live in their own window above everything.
        val focusHeader = focusState.active?.let { active ->
            val name = focusState.profiles.firstOrNull { it.id == active.profileId }?.name
            "${name ?: "Focus"} · ${active.countdown(focusState.now)}"
        }
        if (drawerOpen) {
            DrawerScreen(
                state = appsState,
                onQueryChange = onQueryChange,
                onLaunch = ::handleLaunch,
                loadIcon = loadIcon,
                onSetLevel = onSetLevel,
                focusHeader = focusHeader,
                iconSize = iconSize,
                modifier = Modifier.onSwipeDown { drawerOpen = false }.zIndex(1f),
            )
        }
        if (manageOpen) {
            ManageAppsScreen(
                state = manageState,
                onQueryChange = onManageQuery,
                onSetLevel = onManageSetLevel,
                onSetLimit = onManageSetLimit,
                onClose = { manageOpen = false },
                loadIcon = loadIcon,
                iconSize = iconSize,
                modifier = Modifier.zIndex(2f),
            )
        }
        if (usageOpen) {
            UsagePermissionScreen(
                hasPermission = appsState.hasUsagePermission,
                onOpenSettings = onOpenUsageSettings,
                onClose = { usageOpen = false },
                modifier = Modifier.zIndex(2f),
            )
        }
        if (focusSettingsOpen) {
            FocusSettingsScreen(
                state = focusSettingsState,
                onQueryChange = onFocusSettingsQuery,
                onSelectProfile = onSelectFocusProfile,
                onToggleAllowed = onToggleFocusAllowed,
                onDndToggle = onFocusDndToggle,
                onOpenDndSettings = onOpenDndSettings,
                onSaveSchedule = onSaveFocusSchedule,
                onClearSchedule = onClearFocusSchedule,
                onClose = { focusSettingsOpen = false },
                loadIcon = loadIcon,
                iconSize = iconSize,
                modifier = Modifier.zIndex(2f),
            )
        }
        if (todosOpen) {
            TodosScreen(
                state = todosState,
                onToggle = onTodoToggle,
                onDelete = onTodoDelete,
                onClearCompleted = onTodosClearCompleted,
                onClose = { todosOpen = false },
                modifier = Modifier.zIndex(2f),
            )
        }
        if (notesOpen) {
            NotesScreen(
                state = notesState,
                onTogglePin = onNoteTogglePin,
                onDelete = onNoteDelete,
                onClose = { notesOpen = false },
                modifier = Modifier.zIndex(2f),
            )
        }
        if (homeCustomOpen) {
            HomeCustomizationScreen(
                layout = homeLayout,
                onMove = onCardMove,
                onHidden = onCardHidden,
                onLayout = onLayoutChange,
                onClose = { homeCustomOpen = false },
                modifier = Modifier.zIndex(2f),
            )
        }
        val gated = pendingPause
        if (gated != null) {
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
                modifier = Modifier.zIndex(3f),
            )
        }
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

@Composable
fun MinimalIntention(
    intention: String,
    hasIntention: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!hasIntention) return
    Text(
        text = intention,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

@Composable
fun BlankHome(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
    onManageClick: () -> Unit = {},
    onUsageClick: () -> Unit = {},
    onFocusClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.hello_blank))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            OutlinedButton(onClick = onManageClick) {
                Text(text = stringResource(R.string.manage_apps))
            }
            OutlinedButton(onClick = onUsageClick) {
                Text(text = stringResource(R.string.usage_limits))
            }
            OutlinedButton(onClick = onFocusClick) {
                Text(text = stringResource(R.string.focus_button))
            }
            OutlinedButton(onClick = onHomeClick) {
                Text(text = stringResource(R.string.home_customize))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when {
            !isRoleAvailable -> Text(text = stringResource(R.string.default_launcher_unavailable))
            isDefaultLauncher -> Text(text = stringResource(R.string.is_default_launcher))
            else -> {
                Text(text = stringResource(R.string.not_default_launcher))
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRequestDefault) {
                    Text(text = stringResource(R.string.set_as_default))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlankHomePreview() {
    FocusLauncherTheme {
        BlankHome(
            isRoleAvailable = true,
            isDefaultLauncher = false,
            onRequestDefault = {},
        )
    }
}
