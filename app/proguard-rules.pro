# ProGuard / R8 优化规则
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Media3 ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# Gson — 保护数据模型（含 @SerializedName 枚举）
-keep class com.carousel.app.model.** { *; }
-keepclassmembers class com.carousel.app.model.** { *; }

# DataStore Preferences
-keep class androidx.datastore.preferences.** { *; }
-keepclassmembers class androidx.datastore.preferences.protobuf.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**
