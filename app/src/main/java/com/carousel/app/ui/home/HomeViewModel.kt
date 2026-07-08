package com.carousel.app.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carousel.app.CarouselApp
import com.carousel.app.model.FillMode
import com.carousel.app.model.HomeUiState
import com.carousel.app.model.ImageScaleType
import com.carousel.app.model.MediaFile
import com.carousel.app.model.PlayConfig
import com.carousel.app.util.MAX_MEDIA_COUNT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val configRepo = CarouselApp.instance.configRepository
    private val mediaRepo = CarouselApp.instance.mediaRepository

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        CarouselApp.instance.onMediaPicked = { uris ->
            if (uris.isNotEmpty()) importMedia(uris)
        }
        viewModelScope.launch {
            configRepo.configFlow.collect { c ->
                _state.update { it.copy(mediaFiles = c.mediaFiles, soundEnabled = c.soundEnabled, videoFillMode = c.videoFillMode, imageFillMode = c.imageFillMode, imageDurationSec = c.imageDurationSec) }
            }
        }
        refreshTotalSize()
    }

    fun pickMedia() { Log.e("HomeVM", "pickMedia"); CarouselApp.instance.launchMediaPicker() }

    private fun importMedia(uris: List<Uri>) {
        val slots = MAX_MEDIA_COUNT - _state.value.mediaFiles.size
        if (slots <= 0) { _state.update { it.copy(errorMessage = "最多 $MAX_MEDIA_COUNT 个文件") }; return }
        val toImport = uris.take(slots)
        _state.update { it.copy(isImporting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val items = mediaRepo.importMedia(toImport)
                if (items.isEmpty()) { _state.update { it.copy(isImporting = false, errorMessage = "导入失败") }; return@launch }
                configRepo.updateMediaFiles(_state.value.mediaFiles + items)
                refreshTotalSize()
                _state.update { it.copy(isImporting = false) }
            } catch (e: Exception) { _state.update { it.copy(isImporting = false, errorMessage = "导入出错：${e.message}") } }
        }
    }

    fun removeMedia(item: MediaFile) {
        viewModelScope.launch {
            mediaRepo.deleteMedia(item)
            configRepo.updateMediaFiles(_state.value.mediaFiles.filter { it.id != item.id })
            refreshTotalSize()
        }
    }

    fun reorderMedia(from: Int, to: Int) {
        val list = _state.value.mediaFiles.toMutableList()
        if (from == to) return
        list.add(to, list.removeAt(from))
        _state.update { it.copy(mediaFiles = list) }
        viewModelScope.launch { configRepo.updateMediaFiles(list) }
    }

    fun toggleSound(enabled: Boolean) {
        _state.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch { configRepo.updateSound(enabled) }
    }

    fun setVideoFillMode(mode: FillMode) {
        _state.update { it.copy(videoFillMode = mode) }
        viewModelScope.launch { configRepo.updateVideoFillMode(mode) }
    }

    fun setImageFillMode(mode: ImageScaleType) {
        _state.update { it.copy(imageFillMode = mode) }
        viewModelScope.launch { configRepo.updateImageFillMode(mode) }
    }

    fun setImageDurationSec(sec: Int) {
        _state.update { it.copy(imageDurationSec = sec) }
        viewModelScope.launch { configRepo.updateImageDurationSec(sec) }
    }

    fun startPlayback(): PlayConfig {
        val s = _state.value
        val config = PlayConfig(mediaFiles = s.mediaFiles, soundEnabled = s.soundEnabled, videoFillMode = s.videoFillMode, imageFillMode = s.imageFillMode, imageDurationSec = s.imageDurationSec, wasPlaying = true)
        viewModelScope.launch { configRepo.saveConfig(config) }
        return config
    }

    fun clearError() { _state.update { it.copy(errorMessage = null) } }

    private fun refreshTotalSize() {
        viewModelScope.launch { _state.update { it.copy(totalSizeBytes = mediaRepo.getTotalSize()) } }
    }
}
