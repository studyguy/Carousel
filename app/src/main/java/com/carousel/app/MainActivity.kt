package com.carousel.app

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.carousel.app.model.PlayConfig
import com.carousel.app.ui.home.HomeScreen
import com.carousel.app.ui.player.PlayerScreen
import com.carousel.app.ui.theme.CarouselTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private var isInHomeMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Photo Picker：同时支持图片和视频
        val pickerLauncher = registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
        ) { uris ->
            Log.e("MainActivity", "Picker 返回 ${uris.size} 个文件")
            if (uris.isNotEmpty()) {
                CarouselApp.instance.onMediaPicked?.invoke(uris)
            }
        }
        CarouselApp.instance.setMediaPickerLauncher(pickerLauncher)

        enableEdgeToEdge()

        setContent {
            CarouselTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D0D0D))
                ) {
                    CarouselNavigation(
                        onHomeModeChanged = { home -> isInHomeMode = home }
                    )
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }
}

@Composable
private fun CarouselNavigation(onHomeModeChanged: (Boolean) -> Unit) {
    val configRepo = CarouselApp.instance.configRepository

    var initialConfig by remember { mutableStateOf<PlayConfig?>(null) }

    LaunchedEffect(Unit) {
        initialConfig = configRepo.configFlow.first()
    }

    val showPlayerStart = initialConfig?.wasPlaying == true && initialConfig?.mediaFiles?.isNotEmpty() == true
    var showPlayer by remember { mutableStateOf(showPlayerStart) }

    LaunchedEffect(showPlayer) {
        onHomeModeChanged(!showPlayer)
    }

    val cfg = initialConfig
    if (cfg == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    var playConfig by remember { mutableStateOf(cfg) }

    if (showPlayer) {
        PlayerScreen(
            config = playConfig,
            onGoHome = { showPlayer = false },
        )
    } else {
        HomeScreen(
            onStartPlay = { config ->
                playConfig = config
                showPlayer = true
            },
        )
    }
}
