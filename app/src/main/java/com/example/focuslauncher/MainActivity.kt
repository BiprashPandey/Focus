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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.domain.model.AppLevel
import com.example.focuslauncher.ui.drawer.AppsUiState
import com.example.focuslauncher.ui.drawer.AppsViewModel
import com.example.focuslauncher.ui.drawer.DrawerScreen
import com.example.focuslauncher.ui.focus.FocusCard
import com.example.focuslauncher.ui.focus.FocusSettingsScreen
import com.example.focuslauncher.ui.focus.FocusSettingsViewModel
import com.example.focuslauncher.ui.focus.FocusViewModel
import com.example.focuslauncher.ui.home.DockSection
import com.example.focuslauncher.ui.home.HomeUiState
import com.example.focuslauncher.ui.home.HomeViewModel
import com.example.focuslauncher.ui.manage.ManageAppsScreen
import com.example.focuslauncher.ui.manage.ManageAppsViewModel
import com.example.focuslauncher.ui.pause.PauseScreen
import com.example.focuslauncher.ui.pause.rememberPauseLine
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import com.example.focuslauncher.ui.usage.UsagePermissionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appsViewModel: AppsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val manageViewModel: ManageAppsViewModel by viewModels()
    private val focusViewModel: FocusViewModel by viewModels()
    private val focusSettingsViewModel: FocusSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        setContent {
            FocusLauncherTheme {
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
                    HomeScreen(
                        isRoleAvailable = isHomeRoleAvailable(),
                        isDefaultLauncher = isDefaultLauncher(),
                        onRequestDefault = ::requestHomeRole,
                        appsState = appsState,
                        homeState = homeState,
                        manageState = manageState,
                        focusState = focusState,
                        focusSettingsState = focusSettingsState,
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
    onDockPick: (Int, AppInfo?) -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    var manageOpen by remember { mutableStateOf(false) }
    var usageOpen by remember { mutableStateOf(false) }
    var focusSettingsOpen by remember { mutableStateOf(false) }
    var pendingPause by remember { mutableStateOf<AppInfo?>(null) }
    BackHandler(enabled = drawerOpen) { drawerOpen = false }
    BackHandler(enabled = manageOpen) { manageOpen = false }
    BackHandler(enabled = usageOpen) { usageOpen = false }
    BackHandler(enabled = focusSettingsOpen) { focusSettingsOpen = false }
    fun handleLaunch(app: AppInfo) {
        if (app.packageName in appsState.gatedPackages ||
            app.packageName in appsState.blockedPackages
        ) {
            pendingPause = app
        } else {
            onLaunchApp(app)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            )
            HorizontalDivider()
            FocusCard(
                state = focusState,
                onStart = onStartFocus,
                onStop = onStopFocus,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.swipe_up_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            DockSection(
                dock = homeState.dock,
                allApps = homeState.allApps,
                onLaunch = ::handleLaunch,
                onPick = onDockPick,
                loadIcon = loadIcon,
            )
        }
        if (drawerOpen) {
            DrawerScreen(
                state = appsState,
                onQueryChange = onQueryChange,
                onLaunch = ::handleLaunch,
                loadIcon = loadIcon,
                onSetLevel = onSetLevel,
                modifier = Modifier.onSwipeDown { drawerOpen = false },
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
            )
        }
        if (usageOpen) {
            UsagePermissionScreen(
                hasPermission = appsState.hasUsagePermission,
                onOpenSettings = onOpenUsageSettings,
                onClose = { usageOpen = false },
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
fun BlankHome(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
    onManageClick: () -> Unit = {},
    onUsageClick: () -> Unit = {},
    onFocusClick: () -> Unit = {},
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
