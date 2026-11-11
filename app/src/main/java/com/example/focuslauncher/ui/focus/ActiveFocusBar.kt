package com.example.focuslauncher.ui.focus

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.focus.countdown
import com.example.focuslauncher.domain.focus.remaining
import com.example.focuslauncher.ui.components.GlassCard
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.TimerFontFamily
import com.example.focuslauncher.ui.theme.colors

/**
 * Compact running-session bar pinned above the dock (and mirrored as a line
 * in the drawer), so the timer stays visible no matter which overlay is open.
 */
@Composable
fun ActiveFocusBar(
    active: FocusState.Active,
    now: Long,
    profileName: String?,
    onStop: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Accent.FOCUS
    val colors = accent.colors()
    val total = (active.endsAt - active.startedAt).coerceAtLeast(1L)
    val fraction = (active.remaining(now).toFloat() / total).coerceIn(0f, 1f)
    GlassCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        accent = accent,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulsingDot(color = colors.bright)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = profileName ?: stringResource(R.string.focus_session),
                style = MaterialTheme.typography.titleSmall,
                color = colors.content,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = active.countdown(now),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = TimerFontFamily,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        Spacer(modifier = Modifier.size(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = colors.bright,
            trackColor = colors.container.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.size(6.dp))
        EndFocusControl(active = active, onStop = onStop, showHint = false)
    }
}

@Composable
private fun PulsingDot(color: androidx.compose.ui.graphics.Color) {
    val transition = rememberInfiniteTransition(label = "focusPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .size(9.dp)
            .scale(pulse)
            .background(color, CircleShape),
    )
}
