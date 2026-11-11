package com.example.focuslauncher.ui.pause

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.ui.components.AuroraBackdrop
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.components.GradientButton
import com.example.focuslauncher.ui.components.StatPill
import com.example.focuslauncher.ui.drawer.AppIcon
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors
import kotlinx.coroutines.delay
import java.time.LocalDate

const val PAUSE_SECONDS = 10

@Composable
fun PauseScreen(
    app: AppInfo,
    todayMinutes: Int = 0,
    motivationalLine: String,
    onOpenAnyway: (AppInfo) -> Unit,
    onDoSomethingElse: () -> Unit,
    onClose: () -> Unit,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
) {
    var remaining by remember(app.key) { mutableIntStateOf(PAUSE_SECONDS) }
    LaunchedEffect(app.key) {
        while (remaining > 0) {
            delay(1_000)
            remaining--
        }
    }
    BackHandler { onClose() }

    val accent = Accent.FOCUS
    val colors = accent.colors()
    val ready = remaining <= 0

    Box(modifier = modifier.fillMaxSize()) {
        AuroraBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GlassCard(
                accent = accent,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (ready) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = colors.content,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pause_breathe),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.content,
                        )
                    }
                    Spacer(modifier = Modifier.size(18.dp))
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = { remaining.toFloat() / PAUSE_SECONDS },
                            modifier = Modifier.size(150.dp),
                            color = colors.bright,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                            strokeWidth = 5.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppIcon(
                                app = app,
                                loadIcon = loadIcon,
                                size = 52.dp,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            Text(
                                text = if (ready) {
                                    stringResource(R.string.pause_open_anyway)
                                } else {
                                    stringResource(R.string.pause_wait, remaining)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.content,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.pause_title, app.label),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    StatPill(
                        text = stringResource(R.string.pause_usage_today, todayMinutes),
                        accent = accent,
                    )
                    Spacer(modifier = Modifier.size(22.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.FormatQuote,
                            contentDescription = null,
                            tint = colors.bright,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = motivationalLine,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                        )
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                    GradientButton(
                        text = if (ready) {
                            stringResource(R.string.pause_open_anyway)
                        } else {
                            stringResource(R.string.pause_wait, remaining)
                        },
                        onClick = { onOpenAnyway(app) },
                        enabled = ready,
                        accent = accent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    OutlinedButton(
                        onClick = onDoSomethingElse,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        ),
                    ) {
                        Text(stringResource(R.string.pause_do_else))
                    }
                    TextButton(onClick = onClose) {
                        Text(stringResource(R.string.pause_close))
                    }
                }
            }
        }
    }
}

/** Default line helper so callers don't duplicate date logic. */
@Composable
fun rememberPauseLine(): String = remember {
    com.example.focuslauncher.data.motivation.PauseLines.forDay(LocalDate.now().toEpochDay())
}
