package com.carousel.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carousel.app.model.MediaFile
import com.carousel.app.model.MediaType
import com.carousel.app.ui.theme.DarkSurfaceVariant
import com.carousel.app.ui.theme.DividerDark
import com.carousel.app.ui.theme.TextSecondary
import java.io.File

@Composable
fun MediaListItem(
    item: MediaFile,
    thumbnailPath: String?,
    isDragging: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnail = remember(thumbnailPath) { loadThumbnail(thumbnailPath) }

    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkSurfaceVariant, tonalElevation = if (isDragging) 4.dp else 0.dp) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DragHandle, "拖拽排序", tint = TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))

            Box(Modifier.size(96.dp, 54.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black).border(1.dp, DividerDark, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                if (thumbnail != null) androidx.compose.foundation.Image(thumbnail.asImageBitmap(), item.fileName, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (item.mediaType == MediaType.VIDEO) Icons.Default.Videocam else Icons.Default.Image, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(item.fileName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(2.dp))
                Text(if (item.mediaType == MediaType.VIDEO) formatDuration(item.durationMs) else "图片", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)) }
        }
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "--:--"
    val t = ms / 1000; return "%02d:%02d".format(t / 60, t % 60)
}

private fun loadThumbnail(p: String?): Bitmap? {
    if (p == null || !File(p).exists()) return null
    return try { BitmapFactory.decodeFile(p) } catch (_: Exception) { null }
}
