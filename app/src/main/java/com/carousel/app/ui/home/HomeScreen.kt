package com.carousel.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carousel.app.CarouselApp
import com.carousel.app.model.MediaType
import com.carousel.app.model.PlayConfig
import com.carousel.app.ui.components.MediaListItem
import com.carousel.app.ui.components.ReorderableLazyColumn
import com.carousel.app.ui.components.SettingsPanel
import com.carousel.app.ui.theme.TextSecondary
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartPlay: (PlayConfig) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Column { Text("展厅轮播"); if (state.totalSizeBytes > 0) Text("已占用 ${formatBytes(state.totalSizeBytes)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (state.mediaFiles.isEmpty()) {
                EmptyState(onPick = { viewModel.pickMedia() })
            } else {
                val hasVideos = state.mediaFiles.any { it.mediaType == MediaType.VIDEO }
                val hasImages = state.mediaFiles.any { it.mediaType == MediaType.IMAGE }

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("播放列表（长按拖拽排序）", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

                    ReorderableLazyColumn(
                        items = state.mediaFiles, onMove = { from, to -> viewModel.reorderMedia(from, to) }, modifier = Modifier.heightIn(max = 400.dp),
                    ) { item, isDragging ->
                        MediaListItem(item = item, thumbnailPath = CarouselApp.instance.mediaRepository.thumbnailPath(item.id), isDragging = isDragging, onDelete = { viewModel.removeMedia(item) })
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = { viewModel.pickMedia() }, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("添加更多内容")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    SettingsPanel(
                        soundEnabled = state.soundEnabled,
                        videoFillMode = state.videoFillMode,
                        imageFillMode = state.imageFillMode,
                        imageDurationSec = state.imageDurationSec,
                        hasVideos = hasVideos,
                        hasImages = hasImages,
                        onSoundChange = { viewModel.toggleSound(it) },
                        onVideoFillModeChange = { viewModel.setVideoFillMode(it) },
                        onImageFillModeChange = { viewModel.setImageFillMode(it) },
                        onImageDurationChange = { viewModel.setImageDurationSec(it) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
            if (state.isImporting) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }

        Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
            val n = state.mediaFiles.size
            Button(onClick = { if (n > 0) onStartPlay(viewModel.startPlayback()) }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp), enabled = !state.isImporting) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(24.dp)); Spacer(Modifier.width(8.dp))
                Text(if (n == 0) "请先选择内容" else "开始播放（${n} 个内容）", style = MaterialTheme.typography.labelLarge)
            }
        }
        Box { SnackbarHost(snackbarHostState) }
    }
}

@Composable
private fun EmptyState(onPick: () -> Unit) {
    Box(Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhotoLibrary, null, Modifier.size(72.dp), tint = TextSecondary.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp)); Text("从相册选取播放内容", style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp)); Button(onClick = onPick) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("从相册选择") }
        }
    }
}

private fun formatBytes(bytes: Long) = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))}MB"
    else -> "${"%.2f".format(bytes.toDouble() / (1024 * 1024 * 1024))}GB"
}
