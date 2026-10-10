package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.focuslauncher.R
import com.example.focuslauncher.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Temporary debug section (Step 1.1): proves the repository lists installed
 * apps and refreshes live. Replaced by the real drawer UI in Step 1.2.
 */
@Composable
fun AppsDebugSection(
    state: AppsUiState,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (state.isLoading) {
                stringResource(R.string.loading_apps)
            } else {
                stringResource(R.string.installed_apps, state.apps.size)
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        HorizontalDivider()
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.apps, key = { it.key }) { app ->
                AppDebugRow(app = app, loadIcon = loadIcon)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AppDebugRow(
    app: AppInfo,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppDebugIcon(app = app, loadIcon = loadIcon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Loads the icon off the main thread only when the row is composed. */
@Composable
private fun AppDebugIcon(
    app: AppInfo,
    loadIcon: suspend (AppInfo) -> Drawable?,
) {
    var bitmap by remember(app.key) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.key) {
        val drawable = withContext(Dispatchers.IO) { loadIcon(app) }
        bitmap = drawable?.toImageBitmap()
    }
    val b = bitmap
    if (b != null) {
        Image(
            bitmap = b,
            contentDescription = app.label,
            modifier = Modifier.size(40.dp),
        )
    } else {
        Spacer(modifier = Modifier.size(40.dp))
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    val width = if (intrinsicWidth > 0) intrinsicWidth else FALLBACK_ICON_PX
    val height = if (intrinsicHeight > 0) intrinsicHeight else FALLBACK_ICON_PX
    return toBitmap(width, height).asImageBitmap()
}

private const val FALLBACK_ICON_PX = 96
