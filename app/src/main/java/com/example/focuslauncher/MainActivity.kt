package com.example.focuslauncher

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.ui.theme.FocusLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // On home: back press does nothing (drawer will use it later).
                    BackHandler(enabled = true) { /* stay on home */ }
                    BlankHome(
                        isRoleAvailable = isHomeRoleAvailable(),
                        isDefaultLauncher = isDefaultLauncher(),
                        onRequestDefault = ::requestHomeRole,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // singleTask relaunch (Home press): return to top. Single screen for now,
        // so just update the intent; later phases reset nav to home destination here.
        setIntent(intent)
    }

    private fun roleManagerOrNull(): RoleManager? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return getSystemService(RoleManager::class.java)
    }

    private fun isHomeRoleAvailable(): Boolean {
        val rm = roleManagerOrNull() ?: return false
        return rm.isRoleAvailable(RoleManager.ROLE_HOME)
    }

    private fun isDefaultLauncher(): Boolean {
        val rm = roleManagerOrNull() ?: return false
        return rm.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun requestHomeRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val rm = roleManagerOrNull() ?: return
        if (!rm.isRoleAvailable(RoleManager.ROLE_HOME) || rm.isRoleHeld(RoleManager.ROLE_HOME)) return
        homeRoleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
    }

    private val homeRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Recompose with fresh role state after the system dialog closes.
            setContent {
                FocusLauncherTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        BackHandler(enabled = true) { /* stay on home */ }
                        BlankHome(
                            isRoleAvailable = isHomeRoleAvailable(),
                            isDefaultLauncher = isDefaultLauncher(),
                            onRequestDefault = ::requestHomeRole,
                        )
                    }
                }
            }
        }
}

@Composable
fun BlankHome(
    isRoleAvailable: Boolean,
    isDefaultLauncher: Boolean,
    onRequestDefault: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.hello_blank))
        Spacer(modifier = Modifier.height(16.dp))
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
