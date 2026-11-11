package com.example.focuslauncher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.focuslauncher.domain.model.AppInfo
import com.example.focuslauncher.ui.theme.Accent
import com.example.focuslauncher.ui.theme.colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * App icon that loads off the main thread only when composed. While loading
 * (or if the drawable is missing) a stable tinted letter tile keeps the grid
 * from collapsing or flashing empty.
 */
@Composable
fun AppIcon(
    app: AppInfo,
    loadIcon: suspend (AppInfo) -> Drawable?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val density = LocalDensity.current
    var bitmap by remember(app.key) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.key, size) {
        val drawable = withContext(Dispatchers.IO) { loadIcon(app) }
        val px = with(density) { size.roundToPx() }.coerceIn(1, 192)
        bitmap = drawable?.toImageBitmap(px)
    }
    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(size),
        )
    } else {
        IconFallback(app = app, modifier = modifier.size(size))
    }
}

@Composable
private fun IconFallback(app: AppInfo, modifier: Modifier = Modifier) {
    val accent = remember(app.packageName) {
        Accent.entries[(app.packageName.hashCode() and Int.MAX_VALUE) % Accent.entries.size]
    }
    val colors = accent.colors()
    val initial = app.label.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "•"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.container, colors.bright.copy(alpha = 0.55f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.content,
        )
    }
}

private fun Drawable.toImageBitmap(px: Int): ImageBitmap =
    toBitmap(width = px, height = px).asImageBitmap()
