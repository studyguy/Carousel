package com.carousel.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carousel.app.model.FillMode
import com.carousel.app.model.ImageScaleType
import com.carousel.app.ui.theme.DarkSurfaceVariant
import com.carousel.app.ui.theme.DividerDark
import com.carousel.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

/**
 * 播放设置面板。
 * 声音（始终显示） + 视频设置（有视频时显示） + 图片设置（有图片时显示）。
 */
@Composable
fun SettingsPanel(
    soundEnabled: Boolean,
    videoFillMode: FillMode,
    imageFillMode: ImageScaleType,
    imageDurationSec: Int,
    hasVideos: Boolean,
    hasImages: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onVideoFillModeChange: (FillMode) -> Unit,
    onImageFillModeChange: (ImageScaleType) -> Unit,
    onImageDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("播放设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(16.dp))

        // 声音
        SoundToggle(enabled = soundEnabled, onChange = onSoundChange)

        // 视频设置（有视频才显示）
        if (hasVideos) {
            Spacer(Modifier.height(16.dp))
            VideoSection(mode = videoFillMode, onChange = onVideoFillModeChange)
        }

        // 图片设置（有图片才显示）
        if (hasImages) {
            Spacer(Modifier.height(16.dp))
            ImageSection(mode = imageFillMode, durationSec = imageDurationSec, onModeChange = onImageFillModeChange, onDurationChange = onImageDurationChange)
        }
    }
}

// ---- 声音 ----

@Composable
private fun SoundToggle(enabled: Boolean, onChange: (Boolean) -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = DarkSurfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (enabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, null, tint = if (enabled) MaterialTheme.colorScheme.primary else TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text("声音", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface); Text(if (enabled) "开启" else "静音", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
            Switch(checked = enabled, onCheckedChange = onChange)
        }
    }
}

// ---- 视频设置区 ----

@Composable
private fun VideoSection(mode: FillMode, onChange: (FillMode) -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = DarkSurfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("视频填充模式", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            FillModePreview(currentMode = mode, onSelect = onChange, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---- 图片设置区 ----

@Composable
private fun ImageSection(
    mode: ImageScaleType,
    durationSec: Int,
    onModeChange: (ImageScaleType) -> Unit,
    onDurationChange: (Int) -> Unit,
) {
    Surface(shape = MaterialTheme.shapes.medium, color = DarkSurfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("图片填充模式", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            ImageFillModeSelector(current = mode, onSelect = onModeChange, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Text("图片停留时长", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${durationSec}秒", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Slider(value = durationSec.toFloat(), onValueChange = { onDurationChange(it.roundToInt()) }, valueRange = 1f..30f, steps = 28)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1秒", style = MaterialTheme.typography.bodySmall, color = TextSecondary); Text("30秒", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

/** 图片填充模式 3 选 1 预览。 */
@Composable
private fun ImageFillModeSelector(
    current: ImageScaleType,
    onSelect: (ImageScaleType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // 适应：16:9 内容居中，上下黑边
        ImageModeBox("适应", current == ImageScaleType.FIT_CENTER, onClick = { onSelect(ImageScaleType.FIT_CENTER) }) { w, h ->
            val vh = w * 9f / 16f; val oy = (h - vh) / 2f
            drawRect(Color.Black, Offset.Zero, Size(w, h))
            drawRect(Color(0xFF1A73E8), Offset(0f, oy), Size(w, vh))
            drawRect(Color.White.copy(alpha = 0.4f), Offset(0f, oy), Size(w, vh), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
        }
        // 铺满：四宫格蓝色方块平铺整个盒子，仅留细缝
        ImageModeBox("铺满", current == ImageScaleType.CENTER_CROP, onClick = { onSelect(ImageScaleType.CENTER_CROP) }) { w, h ->
            val gap = 2f
            val cw = (w - gap) / 2f
            val ch = (h - gap) / 2f
            drawRect(Color(0xFF1A73E8), Offset(0f, 0f), Size(cw, ch))
            drawRect(Color(0xFF4DA6FF), Offset(cw + gap, 0f), Size(cw, ch))
            drawRect(Color(0xFF4DA6FF), Offset(0f, ch + gap), Size(cw, ch))
            drawRect(Color(0xFF1A73E8), Offset(cw + gap, ch + gap), Size(cw, ch))
        }
        // 拉伸：全屏填充
        ImageModeBox("拉伸", current == ImageScaleType.FIT_XY, onClick = { onSelect(ImageScaleType.FIT_XY) }) { w, h ->
            drawRect(Color(0xFF1A73E8), Offset.Zero, Size(w, h))
        }
    }
}

@Composable
private fun ImageModeBox(
    label: String, selected: Boolean, onClick: () -> Unit,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.(w: Float, h: Float) -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else DividerDark
    val borderWidth = if (selected) 2.dp else 1.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(100.dp, 72.dp).clip(RoundedCornerShape(8.dp)).border(borderWidth, borderColor, RoundedCornerShape(8.dp))) {
            Canvas(Modifier.fillMaxSize()) { onDraw(size.width, size.height) }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = if (selected) MaterialTheme.colorScheme.primary else TextSecondary, textAlign = TextAlign.Center)
    }
}
