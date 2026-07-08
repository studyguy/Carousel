package com.carousel.app

import android.app.Application
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class CarouselApp : Application() {

    lateinit var configRepository: com.carousel.app.data.ConfigRepository
        private set

    lateinit var mediaRepository: com.carousel.app.data.MediaRepository
        private set

    /** 媒体选择器启动器 — 由 MainActivity 注入 */
    var mediaPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
        private set

    /** 选择完成后的回调 */
    var onMediaPicked: ((List<Uri>) -> Unit)? = null

    fun setMediaPickerLauncher(launcher: ActivityResultLauncher<PickVisualMediaRequest>) {
        mediaPickerLauncher = launcher
    }

    /** 拉起媒体选择器（视频 + 图片） */
    fun launchMediaPicker() {
        mediaPickerLauncher?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        configRepository = com.carousel.app.data.ConfigRepository(this)
        mediaRepository = com.carousel.app.data.MediaRepository(this)
    }

    companion object {
        lateinit var instance: CarouselApp
            private set
    }
}
