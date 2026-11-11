package com.example.focuslauncher

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.focuslauncher.data.theme.ColorSources
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.ui.calendar.CalendarViewModel
import com.example.focuslauncher.ui.drawer.AppsViewModel
import com.example.focuslauncher.ui.focus.FocusSettingsViewModel
import com.example.focuslauncher.ui.focus.FocusViewModel
import com.example.focuslauncher.ui.home.HomeSettingsViewModel
import com.example.focuslauncher.ui.home.HomeScreen
import com.example.focuslauncher.ui.home.HomeViewModel
import com.example.focuslauncher.ui.manage.ManageAppsViewModel
import com.example.focuslauncher.ui.motivation.MotivationViewModel
import com.example.focuslauncher.ui.notes.NotesViewModel
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import com.example.focuslauncher.ui.theme.ThemeViewModel
import com.example.focuslauncher.ui.theme.isDarkTheme
import com.example.focuslauncher.ui.todos.TodosViewModel
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
        enableEdgeToEdge()
        render()
    }

    private fun render() {
        setContent {
            val themeConfig by themeViewModel.config.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = isDarkTheme(themeConfig, systemDark)
            val view = LocalView.current
            DisposableEffect(dark, view) {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                onDispose { }
            }
            FocusLauncherTheme(config = themeConfig) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    // On home: back press does nothing (overlays handle their own back).
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
                        themeConfig = themeConfig,
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
                        onRefreshFocusPermissions = focusSettingsViewModel::refreshDndAccess,
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
                        onThemeMode = themeViewModel::setMode,
                        onThemeSource = themeViewModel::setSource,
                        onThemePreset = themeViewModel::applyPreset,
                        onThemeSeedHex = ::applySeedHex,
                        onThemeCorner = themeViewModel::setCorner,
                        onThemeIconSize = themeViewModel::setIconSize,
                        onThemeReset = themeViewModel::reset,
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
            pendingDndEnable = true
            openDndSettings()
            return
        }
        pendingDndEnable = false
        focusSettingsViewModel.onDndToggle(enabled)
    }

    private fun applySeedHex(argb: Long) {
        themeViewModel.setSeed(argb)
        themeViewModel.setSource(ColorSources.CUSTOM)
    }

    private fun hasDndAccess(): Boolean =
        getSystemService(android.app.NotificationManager::class.java)
            ?.isNotificationPolicyAccessGranted == true

    private var pendingDndEnable = false

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
        focusSettingsViewModel.refreshDndAccess()
        if (pendingDndEnable && hasDndAccess()) {
            pendingDndEnable = false
            focusSettingsViewModel.onDndToggle(true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
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
