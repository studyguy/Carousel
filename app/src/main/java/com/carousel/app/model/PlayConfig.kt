package com.carousel.app.model

import com.google.gson.annotations.SerializedName

enum class FillMode {
    @SerializedName("fit")  FIT,
    @SerializedName("fill") FILL,
}

data class PlayConfig(
    @SerializedName("mediaFiles")
    val mediaFiles: List<MediaFile> = emptyList(),
    val currentIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val soundEnabled: Boolean = true,
    /** 视频填充模式 */
    val videoFillMode: FillMode = FillMode.FIT,
    /** 图片填充模式（全局统一） */
    val imageFillMode: ImageScaleType = ImageScaleType.FIT_CENTER,
    val wasPlaying: Boolean = false,
    val imageDurationSec: Int = 5,
)
