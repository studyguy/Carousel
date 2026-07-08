package com.carousel.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * 支持长按拖拽排序的 LazyColumn。
 *
 * @param items 数据列表
 * @param onMove 排序后回调 (fromIndex, toIndex)
 * @param itemContent 每个 item 的 Composable（接收 isDragging 参数用于视觉反馈）
 */
@Composable
fun <T> ReorderableLazyColumn(
    modifier: Modifier = Modifier,
    items: List<T>,
    onMove: (Int, Int) -> Unit,
    itemContent: @Composable (T, Boolean) -> Unit,
) {
    val listState = rememberLazyListState()

    // 预计算 item 像素高度（在 composition 中计算）
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 88.dp.toPx() }

    // 拖拽状态
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        itemsIndexed(items, key = { i, _ -> i }) { index, item ->
            val isDragging = index == draggedItemIndex

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 10f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        shadowElevation = if (isDragging) 8f else 0f
                        scaleX = if (isDragging) 1.03f else 1f
                        scaleY = if (isDragging) 1.03f else 1f
                    }
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedItemIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                val toIndex = (index + (dragOffset / itemHeightPx).roundToInt())
                                    .coerceIn(0, items.size - 1)
                                if (toIndex != index) {
                                    onMove(index, toIndex)
                                }
                                draggedItemIndex = -1
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedItemIndex = -1
                                dragOffset = 0f
                            },
                            onDrag = { change, offset ->
                                change.consume()
                                dragOffset += offset.y
                            },
                        )
                    }
            ) {
                itemContent(item, isDragging)
            }
        }
    }
}
