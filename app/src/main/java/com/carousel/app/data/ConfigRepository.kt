package com.carousel.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carousel.app.model.FillMode
import com.carousel.app.model.ImageScaleType
import com.carousel.app.model.MediaFile
import com.carousel.app.model.PlayConfig
import com.carousel.app.util.DATASTORE_NAME
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class ConfigRepository(private val context: Context) {

    private val gson = Gson()
    private val dataStore = context.dataStore

    private object Keys {
        val PLAY_CONFIG = stringPreferencesKey("play_config")
    }

    val configFlow: Flow<PlayConfig> = dataStore.data.map { it[Keys.PLAY_CONFIG]?.let(::decodeConfig) ?: PlayConfig() }

    suspend fun saveConfig(config: PlayConfig) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(config) }
    }

    suspend fun updateMediaFiles(files: List<MediaFile>) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(mediaFiles = files)) }
    }

    suspend fun updateProgress(index: Int, positionMs: Long) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(currentIndex = index, currentPositionMs = positionMs)) }
    }

    suspend fun updateSound(enabled: Boolean) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(soundEnabled = enabled)) }
    }

    suspend fun updateVideoFillMode(mode: FillMode) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(videoFillMode = mode)) }
    }

    suspend fun updateImageFillMode(mode: ImageScaleType) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(imageFillMode = mode)) }
    }

    suspend fun updateImageDurationSec(sec: Int) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(imageDurationSec = sec)) }
    }

    suspend fun setPlayingState(playing: Boolean) {
        dataStore.edit { it[Keys.PLAY_CONFIG] = gson.toJson(decode(it[Keys.PLAY_CONFIG]).copy(wasPlaying = playing)) }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    private fun decode(json: String?) = decodeConfig(json)
    private fun decodeConfig(json: String?): PlayConfig {
        if (json == null) return PlayConfig()
        return try { gson.fromJson(json, PlayConfig::class.java) } catch (_: Exception) { PlayConfig() }
    }
}
