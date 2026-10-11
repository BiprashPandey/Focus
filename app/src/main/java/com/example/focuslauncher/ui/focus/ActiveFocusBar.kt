package com.example.focuslauncher.ui.focus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.focus.FocusState
import com.example.focuslauncher.domain.focus.countdown

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
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profileName ?: stringResource(R.string.focus_session),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = active.countdown(now),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            EndFocusControl(active = active, onStop = onStop, showHint = false)
        }
    }
}
