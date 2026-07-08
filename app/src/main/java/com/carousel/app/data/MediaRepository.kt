package com.carousel.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.carousel.app.model.MediaFile
import com.carousel.app.model.MediaType
import com.carousel.app.util.MEDIA_DIR
import com.carousel.app.util.THUMBNAILS_DIR
import com.carousel.app.util.THUMBNAIL_MAX_SIDE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaRepository(private val context: Context) {

    private val mediaDir: File get() = File(context.filesDir, MEDIA_DIR).also { it.mkdirs() }
    private val thumbnailsDir: File get() = File(context.cacheDir, THUMBNAILS_DIR).also { it.mkdirs() }

    suspend fun importMedia(uris: List<Uri>): List<MediaFile> = withContext(Dispatchers.IO) {
        uris.mapNotNull { importSingle(it) }
    }

    suspend fun deleteMedia(item: MediaFile) = withContext(Dispatchers.IO) {
        File(item.filePath).delete()
        thumbnailFile(item.id).delete()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        mediaDir.listFiles()?.forEach { it.delete() }
        thumbnailsDir.listFiles()?.forEach { it.delete() }
    }

    suspend fun getTotalSize(): Long = withContext(Dispatchers.IO) {
        mediaDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun thumbnailPath(mediaId: String): String = thumbnailFile(mediaId).absolutePath

    // ---- internal ----

    private fun importSingle(uri: Uri): MediaFile? {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isImage = mimeType.startsWith("image/")
            val isVideo = mimeType.startsWith("video/")
            if (!isImage && !isVideo) { Log.e("MediaRepo", "skip $mimeType"); return null }

            val ext = if (isImage) ".jpg" else ".mp4"
            val name = queryFileName(uri) ?: "media_${System.currentTimeMillis()}$ext"
            val target = File(mediaDir, "${System.currentTimeMillis()}_$name")

            val ok = context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { out -> input.copyTo(out) } }
            if (ok == null) { Log.e("MediaRepo", "openInputStream null"); return null }

            Log.e("MediaRepo", "imported $name → ${target.length()} bytes, $mimeType")

            if (isImage) {
                val item = MediaFile(fileName = name, filePath = target.absolutePath, mediaType = MediaType.IMAGE)
                genImageThumb(item); item
            } else {
                val dur = getVideoDuration(target.absolutePath)
                val item = MediaFile(fileName = name, filePath = target.absolutePath, mediaType = MediaType.VIDEO, durationMs = dur)
                genVideoThumb(item); item
            }
        } catch (e: Exception) { Log.e("MediaRepo", "import fail", e); null }
    }

    private fun queryFileName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) { val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (i >= 0) return c.getString(i)?.takeIf { it.isNotBlank() } }
        }; return null
    }

    private fun getVideoDuration(p: String): Long = try {
        MediaMetadataRetriever().use { r -> r.setDataSource(p); r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L }
    } catch (_: Exception) { 0L }

    private fun genVideoThumb(item: MediaFile) {
        try { MediaMetadataRetriever().use { r -> r.setDataSource(item.filePath); r.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST)?.let { b -> saveThumb(b, item.id); b.recycle() } } } catch (_: Exception) {}
    }

    private fun genImageThumb(item: MediaFile) {
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(item.filePath, opts)
            var s = 1; while (opts.outWidth / s > THUMBNAIL_MAX_SIDE || opts.outHeight / s > THUMBNAIL_MAX_SIDE) s *= 2
            BitmapFactory.decodeFile(item.filePath, BitmapFactory.Options().apply { inSampleSize = s })?.let { saveThumb(it, item.id); it.recycle() }
        } catch (_: Exception) {}
    }

    private fun saveThumb(b: Bitmap, id: String) {
        FileOutputStream(thumbnailFile(id)).use { b.compress(Bitmap.CompressFormat.JPEG, 80, it) }
    }

    private fun thumbnailFile(mediaId: String) = File(thumbnailsDir, "$mediaId.jpg")
}