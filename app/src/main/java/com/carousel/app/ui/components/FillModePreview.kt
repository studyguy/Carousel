package com.carousel.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.carousel.app.ui.theme.DividerDark
import com.carousel.app.ui.theme.TextSecondary

/**
 * 画面填充模式预览缩略图。点击图片即可选择模式。
 * 直观展示 FIT（保持比例，上下黑边）vs FILL（拉伸铺满）两种模式的效果差异。
 */
@Composable
fun FillModePreview(
    currentMode: FillMode,
    onSelect: (FillMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 保持比例：宽幅画布凸显上下黑边
        PreviewBox(
            label = "保持比例",
            isSelected = currentMode == FillMode.FIT,
            onClick = { onSelect(FillMode.FIT) },
        ) { canvasWidth, canvasHeight ->
            // 16:9 视频等比缩放，上下留黑边
            val videoHeight = canvasWidth * 9f / 16f
            val offsetY = (canvasHeight - videoHeight) / 2f

            // 全黑背景
            drawRect(Color.Black, Offset.Zero, Size(canvasWidth, canvasHeight))
            // 视频内容（带网格线的蓝色区域）
            drawRect(
                Color(0xFF1A73E8),
                Offset(0f, offsetY),
                Size(canvasWidth, videoHeight),
            )
            // 视频区域白色边框
            drawRect(
                Color.White.copy(alpha = 0.4f),
                Offset(0f, offsetY),
                Size(canvasWidth, videoHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }

        // 拉伸铺满：全屏填充
        PreviewBox(
            label = "拉伸铺满",
            isSelected = currentMode == FillMode.FILL,
            onClick = { onSelect(FillMode.FILL) },
        ) { canvasWidth, canvasHeight ->
            // FILL: 内容拉伸至完全填充画布
            drawRect(
                Color(0xFF1A73E8),
                Offset.Zero,
                Size(canvasWidth, canvasHeight),
            )
        }
    }
}

@Composable
private fun PreviewBox(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.(width: Float, height: Float) -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else DividerDark
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 72.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                onDraw(size.width, size.height)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
