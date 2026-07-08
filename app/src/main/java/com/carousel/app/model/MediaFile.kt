package com.carousel.app.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

enum class MediaType {
    @SerializedName("video") VIDEO,
    @SerializedName("image") IMAGE,
}

/** 图片填充模式（全局统一设置）。 */
enum class ImageScaleType {
    @SerializedName("fit_center") FIT_CENTER,
    @SerializedName("center_crop") CENTER_CROP,
    @SerializedName("fit_xy")  FIT_XY,
}

/** 单个媒体条目。 */
data class MediaFile(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val mediaType: MediaType = MediaType.VIDEO,
    val durationMs: Long = 0L,
)
