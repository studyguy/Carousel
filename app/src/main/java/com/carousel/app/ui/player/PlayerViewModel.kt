package com.carousel.app.ui.player

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.carousel.app.CarouselApp
import com.carousel.app.model.FillMode
import com.carousel.app.model.MediaFile
import com.carousel.app.model.MediaType
import com.carousel.app.model.PlayConfig
import com.carousel.app.util.PROGRESS_SAVE_INTERVAL_MS
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class PlayerViewModel : ViewModel() {

    private val configRepo = CarouselApp.instance.configRepository

    /** 全部播放列表 */
    private var mediaFiles: List<MediaFile> = emptyList()
    private var totalCount = 0

    /** 当前播放索引 */
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /** 当前媒体项 */
    val currentItem: MediaFile?
        get() = mediaFiles.getOrNull(_currentIndex.value)

    /** ExoPlayer — 仅用于视频 */
    private val _exoPlayer = MutableStateFlow<ExoPlayer?>(null)
    val exoPlayer: StateFlow<ExoPlayer?> = _exoPlayer.asStateFlow()

    /** 图片定时器剩余秒数（for UI countdown） */
    private val _imageCountdown = MutableStateFlow(0)
    val imageCountdown: StateFlow<Int> = _imageCountdown.asStateFlow()

    /** 是否正在播放图片 */
    private val _isShowingImage = MutableStateFlow(false)
    val isShowingImage: StateFlow<Boolean> = _isShowingImage.asStateFlow()

    private var currentConfig: PlayConfig? = null
    private var progressSaveJob: Job? = null
    private var imageTimerJob: Job? = null
    private var consecutiveErrors = 0

    fun initialize(config: PlayConfig) {
        releaseAll()
        currentConfig = config
        consecutiveErrors = 0
        mediaFiles = config.mediaFiles
        totalCount = mediaFiles.size
        if (totalCount == 0) return

        _currentIndex.value = config.currentIndex.coerceIn(0, totalCount - 1)
        playCurrent()
    }

    /** 播放当前索引的媒体 */
    private fun playCurrent() {
        val item = mediaFiles.getOrNull(_currentIndex.value) ?: return
        when (item.mediaType) {
            MediaType.VIDEO -> playVideo(item)
            MediaType.IMAGE -> showImage(item)
        }
    }

    // ---- 视频播放 ----

    private fun playVideo(item: MediaFile) {
        _isShowingImage.value = false
        _isShowingImage.value = false // 不展示图片
        val file = File(item.filePath)
        if (!file.exists()) {
            Log.e("PlayerVM", "视频文件不存在: ${item.filePath}，跳过")
            advanceToNext()
            return
        }

        val exoPlayer = ExoPlayer.Builder(CarouselApp.instance)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("PlayerVM", "播放错误: ${error.message}", error)
                        consecutiveErrors++
                        if (consecutiveErrors >= 3) {
                            consecutiveErrors = 0
                            advanceToNext()
                        }
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            // 视频播放完毕 → 下一个
                            advanceToNext()
                        }
                    }
                })
                setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                repeatMode = Player.REPEAT_MODE_OFF
                volume = if (currentConfig?.soundEnabled == true) 1f else 0f
                playWhenReady = true
                prepare()
            }

        _exoPlayer.value = exoPlayer
        startProgressTracking()
    }

    fun resizeMode(fillMode: FillMode): Int = resizeModeStatic(fillMode)

    companion object {
        fun resizeModeStatic(fillMode: FillMode): Int = when (fillMode) {
            FillMode.FIT -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            FillMode.FILL -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _exoPlayer.value?.volume = if (enabled) 1f else 0f
        viewModelScope.launch {
            configRepo.updateSound(enabled)
        }
    }

    fun setFillMode(mode: FillMode) {
        currentConfig = currentConfig?.copy(videoFillMode = mode)
        viewModelScope.launch {
            configRepo.updateVideoFillMode(mode)
        }
    }

    // ---- 图片展示 ----

    private fun showImage(@Suppress("UNUSED_PARAMETER") item: MediaFile) {
        _isShowingImage.value = true
        _exoPlayer.value?.release()
        _exoPlayer.value = null
        consecutiveErrors = 0

        val durationSec = currentConfig?.imageDurationSec ?: 5
        _imageCountdown.value = durationSec

        imageTimerJob?.cancel()
        imageTimerJob = viewModelScope.launch {
            while (isActive && durationSec > 0) {
                delay(1000L)
                val current = _imageCountdown.value
                if (current > 1) {
                    _imageCountdown.value = current - 1
                } else {
                    _imageCountdown.value = 0
                    advanceToNext()
                    break
                }
            }
        }
    }

    /** 手动前进到下一项 */
    fun advanceToNext() {
        val count = totalCount
        if (count == 0) return

        // 保存视频进度
        saveCurrentProgress()

        val next = (_currentIndex.value + 1) % count
        _currentIndex.value = next

        // 释放当前视频
        stopVideo()

        playCurrent()
    }

    /** 手动回退到上一项 */
    fun advanceToPrevious() {
        val count = totalCount
        if (count == 0) return

        val prev = if (_currentIndex.value == 0) count - 1 else _currentIndex.value - 1
        _currentIndex.value = prev

        stopVideo()
        playCurrent()
    }

    private fun stopVideo() {
        imageTimerJob?.cancel()
        imageTimerJob = null
        progressSaveJob?.cancel()
        progressSaveJob = null
        _exoPlayer.value?.apply {
            stop()
            release()
        }
        _exoPlayer.value = null
        _isShowingImage.value = false
    }

    // ---- 进度保存 ----

    private fun saveCurrentProgress() {
        val player = _exoPlayer.value
        if (player != null) {
            val pos = player.currentPosition
            viewModelScope.launch {
                configRepo.updateProgress(_currentIndex.value, pos)
            }
        } else {
            viewModelScope.launch {
                configRepo.updateProgress(_currentIndex.value, 0L)
            }
        }
    }

    private fun startProgressTracking() {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(PROGRESS_SAVE_INTERVAL_MS)
                saveCurrentProgress()
            }
        }
    }

    // ---- 生命周期 ----

    fun markStopped() {
        imageTimerJob?.cancel()
        imageTimerJob = null
        saveCurrentProgress()
        releaseAll()
        viewModelScope.launch {
            configRepo.setPlayingState(false)
        }
    }

    private fun releaseAll() {
        progressSaveJob?.cancel()
        progressSaveJob = null
        imageTimerJob?.cancel()
        imageTimerJob = null
        _exoPlayer.value?.apply {
            stop()
            release()
        }
        _exoPlayer.value = null
        _isShowingImage.value = false
    }

    override fun onCleared() {
        releaseAll()
        super.onCleared()
    }
}
