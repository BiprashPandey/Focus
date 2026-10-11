package com.example.focuslauncher.ui.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
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

    val breath by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.pause_title, app.label),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(breath)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (remaining > 0) remaining.toString() else "✓",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.pause_breathe),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.pause_usage_today, todayMinutes),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = motivationalLine,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onOpenAnyway(app) },
                enabled = remaining <= 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (remaining > 0) {
                        stringResource(R.string.pause_wait, remaining)
                    } else {
                        stringResource(R.string.pause_open_anyway)
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onDoSomethingElse, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pause_do_else))
            }
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.pause_close))
            }
        }
    }
}

/** Default line helper so callers don't duplicate date logic. */
@Composable
fun rememberPauseLine(): String = remember {
    com.example.focuslauncher.data.motivation.PauseLines.forDay(LocalDate.now().toEpochDay())
}
