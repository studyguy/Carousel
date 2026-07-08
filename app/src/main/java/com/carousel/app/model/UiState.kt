package com.carousel.app.model

data class HomeUiState(
    val mediaFiles: List<MediaFile> = emptyList(),
    val totalSizeBytes: Long = 0L,
    val soundEnabled: Boolean = true,
    val videoFillMode: FillMode = FillMode.FIT,
    val imageFillMode: ImageScaleType = ImageScaleType.FIT_CENTER,
    val imageDurationSec: Int = 5,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
)
