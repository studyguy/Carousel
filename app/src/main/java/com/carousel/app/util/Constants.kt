package com.carousel.app.util

/** 用于定时保存播放进度的间隔 */
const val PROGRESS_SAVE_INTERVAL_MS = 5_000L

/** DataStore 文件名 */
const val DATASTORE_NAME = "carousel_config"

/** 媒体存储子目录（位于 filesDir 下，视频和图片共用） */
const val MEDIA_DIR = "media"

/** 缩略图缓存子目录（位于 cacheDir 下） */
const val THUMBNAILS_DIR = "thumbnails"

/** 最大可选媒体数量（软上限） */
const val MAX_MEDIA_COUNT = 50

/** 图片缩略图最大边长 */
const val THUMBNAIL_MAX_SIDE = 320

/** 默认图片停留秒数 */
const val DEFAULT_IMAGE_DURATION_SEC = 5
