package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.focuslauncher.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** App icon that loads off the main thread only when composed. */
@Composable
fun AppIcon(
    app: AppInfo,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
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
            modifier = modifier.size(40.dp),
        )
    } else {
        Spacer(modifier = modifier.size(40.dp))
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    val width = if (intrinsicWidth > 0) intrinsicWidth else FALLBACK_ICON_PX
    val height = if (intrinsicHeight > 0) intrinsicHeight else FALLBACK_ICON_PX
    return toBitmap(width, height).asImageBitmap()
}

private const val FALLBACK_ICON_PX = 96
