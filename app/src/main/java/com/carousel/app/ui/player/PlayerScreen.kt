package com.carousel.app.ui.player

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.carousel.app.model.PlayConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private class ControlsState {
    var hideJob: Job? = null
}

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun PlayerScreen(
    config: PlayConfig,
    onGoHome: () -> Unit,
    viewModel: PlayerViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.initialize(config) }

    // ---- 常亮（竖屏，不隐藏系统栏） ----
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    // ---- 状态 ----
    val isShowingImage by viewModel.isShowingImage.collectAsState()
    val exoPlayer by viewModel.exoPlayer.collectAsState()
    val currentItem = viewModel.currentItem
    var currentFillMode by remember { mutableStateOf(config.videoFillMode) }

    BackHandler { viewModel.markStopped(); onGoHome() }

    // ---- 控件显隐 ----
    var showControls by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val controlsState = remember { ControlsState() }

    val toggleControls: () -> Unit = {
        if (showControls) {
            showControls = false
            controlsState.hideJob?.cancel()
            controlsState.hideJob = null
        } else {
            showControls = true
            controlsState.hideJob?.cancel()
            controlsState.hideJob = scope.launch {
                delay(3000L)
                showControls = false
            }
        }
    }
    val refreshControlsTimer: () -> Unit = {
        showControls = true
        controlsState.hideJob?.cancel()
        controlsState.hideJob = scope.launch {
            delay(3000L)
            showControls = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // ================ 内容区（始终满屏，不随控件显隐缩放） ================
        if (isShowingImage && currentItem != null) {
            ImageDisplay(
                item = currentItem,
                imageFillMode = config.imageFillMode,
                onTap = toggleControls,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (exoPlayer != null) {
            VideoPlayerView(
                exoPlayer = exoPlayer as androidx.media3.exoplayer.ExoPlayer,
                fillMode = currentFillMode,
                showControls = showControls,
                onTap = toggleControls,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ================ 控件叠加层（内容区之上，适配刘海/导航栏） ================
        if (showControls) {
            // 左上返回（避开刘海）
            NativeButton("← 返回", { viewModel.markStopped(); onGoHome() },
                Modifier.align(Alignment.TopStart).windowInsetsPadding(WindowInsets.safeDrawing).padding(start = 12.dp, top = 8.dp))

            // 右上倒计时
            if (isShowingImage && (viewModel.imageCountdown.collectAsState().value) > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.safeDrawing).padding(end = 12.dp, top = 8.dp)
                        .background(Color.Black.copy(0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) { Text("${viewModel.imageCountdown.collectAsState().value}s", color = Color.White, fontSize = 14.sp) }
            }

            // 底部控件栏（避开导航栏）
            BottomControlBar(
                onPrevious = { viewModel.advanceToPrevious(); refreshControlsTimer() },
                onNext = { viewModel.advanceToNext(); refreshControlsTimer() },
                modifier = Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing),
            )
        }
    }
}

/** 底部控件栏：上一张 | 渐变遮罩 | 下一张 */
@Composable
private fun BottomControlBar(onPrevious: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f), Color.Transparent))),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NativeButton("◀ 上一张", onPrevious, Modifier)
            NativeButton("下一张 ▶", onNext, Modifier)
        }
    }
}

/** ExoPlayer + 触摸切换显隐 */
@Composable
private fun VideoPlayerView(
    exoPlayer: androidx.media3.exoplayer.ExoPlayer,
    fillMode: com.carousel.app.model.FillMode,
    showControls: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                resizeMode = com.carousel.app.ui.player.PlayerViewModel.resizeModeStatic(fillMode)
                useController = true
                controllerAutoShow = false
                setBackgroundColor(android.graphics.Color.BLACK)
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) onTap()
                    false
                }
            }
        },
        update = { pv ->
            pv.resizeMode = com.carousel.app.ui.player.PlayerViewModel.resizeModeStatic(fillMode)
            if (showControls) pv.showController() else pv.hideController()
        },
        modifier = modifier,
    )
}

@Composable
private fun NativeButton(text: String, onClick: () -> Unit, modifier: Modifier) {
    AndroidView(
        factory = { ctx ->
            val d = ctx.resources.displayMetrics.density
            TextView(ctx).apply {
                this.text = text
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                val px = (10 * d).toInt()
                val pw = (20 * d).toInt()
                setPadding(pw, px, pw, px)
                setOnClickListener { onClick() }
                background = GradientDrawable().apply {
                    setColor(0x8C000000.toInt())
                    cornerRadius = 24f * d
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ImageDisplay(
    item: com.carousel.app.model.MediaFile,
    imageFillMode: com.carousel.app.model.ImageScaleType,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentScale = when (imageFillMode) {
        com.carousel.app.model.ImageScaleType.FIT_CENTER -> ContentScale.Fit
        com.carousel.app.model.ImageScaleType.CENTER_CROP -> ContentScale.Crop
        com.carousel.app.model.ImageScaleType.FIT_XY -> ContentScale.FillBounds
    }
    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onTap() },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(File(item.filePath)).crossfade(true).build(),
            contentDescription = item.fileName,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
        )
    }
}
