import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.carousel.app"
    compileSdk = 34

    // ---- Signing - 从 local.properties 读取凭据 ----
    val localProps = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }
            ?.let { FileInputStream(it).use { fis -> load(fis) } }
    }
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(localProps.getProperty("storeFile", "carousel.keystore"))
            storePassword = localProps.getProperty("storePassword", "")
            keyAlias = localProps.getProperty("keyAlias", "carousel")
            keyPassword = localProps.getProperty("keyPassword", "")
        }
    }

    defaultConfig {
        applicationId = "com.carousel.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose BOM — 统一管理 Compose 版本
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity & Lifecycle
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Media3 ExoPlayer — 视频播放
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")

    // DataStore — 配置持久化
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Gson — JSON 序列化
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coil — 图片加载（Compose 集成）
    implementation("io.coil-kt:coil-compose:2.5.0")
}
