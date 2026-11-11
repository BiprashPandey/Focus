package com.example.focuslauncher.ui.usage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.focuslauncher.R
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.components.ScreenHeader
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors

@Composable
fun UsagePermissionScreen(
    hasPermission: Boolean,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onClose() }
    val accent = Accent.CALENDAR
    val colors = accent.colors()
    val currentRefresh by rememberUpdatedState(onRefresh)
    LaunchedEffect(Unit) { currentRefresh() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) currentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop(intensity = 0.8f)
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.usage_limits),
                subtitle = stringResource(R.string.usage_subtitle),
                onClose = onClose,
            )
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                GlassCard(accent = accent, contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (hasPermission) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.BarChart
                            },
                            contentDescription = null,
                            tint = colors.bright,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(
                                if (hasPermission) {
                                    R.string.usage_granted_title
                                } else {
                                    R.string.usage_title
                                },
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = stringResource(
                                if (hasPermission) {
                                    R.string.usage_granted_body
                                } else {
                                    R.string.usage_body
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.size(18.dp))
                        StatPill(
                            text = stringResource(
                                if (hasPermission) {
                                    R.string.usage_status_granted
                                } else {
                                    R.string.usage_status_missing
                                },
                            ),
                            accent = if (hasPermission) Accent.FOCUS else null,
                        )
                        Spacer(modifier = Modifier.size(24.dp))
                        GradientButton(
                            text = stringResource(R.string.usage_open_settings),
                            onClick = onOpenSettings,
                            accent = accent,
                            icon = Icons.Rounded.Settings,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        TextButton(
                            onClick = onRefresh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.usage_recheck))
                        }
                        Spacer(modifier = Modifier.size(2.dp))
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            ),
                        ) {
                            Text(stringResource(R.string.manage_apps_close))
                        }
                    }
                }
            }
        }
    }
}
