# 展厅轮播 App

全屏视频循环轮播工具，适用于展厅/展会/门店等固定展位的无人值守循环播放。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **视频**: Media3 ExoPlayer
- **持久化**: DataStore + Gson
- **最低版本**: Android 10 (API 29)

## 快速开始

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择目标设备，点击 Run

## 项目结构

```
app/src/main/java/com/carousel/app/
├── CarouselApp.kt          # Application
├── MainActivity.kt         # 单 Activity + 导航
├── model/
│   ├── VideoItem.kt        # 视频条目
│   ├── PlayConfig.kt       # 播放配置 + FillMode
│   └── UiState.kt          # UI 状态
├── data/
│   ├── ConfigRepository.kt # 配置持久化（DataStore）
│   └── VideoRepository.kt  # 视频文件管理
├── ui/
│   ├── theme/              # Material 3 暗色主题
│   ├── home/
│   │   ├── HomeScreen.kt   # 首页（配置模式）
│   │   └── HomeViewModel.kt
│   ├── player/
│   │   ├── PlayerScreen.kt # 全屏播放
│   │   └── PlayerViewModel.kt
│   └── components/
│       ├── ThreeFingerDetector.kt   # 三指双击检测
│       ├── ReorderableLazyColumn.kt # 拖拽排序列表
│       ├── VideoListItem.kt         # 视频列表项
│       ├── SettingsPanel.kt         # 声音 + 填充模式
│       ├── FillModePreview.kt       # 画幅预览
│       └── UnlockOverlay.kt         # 解锁覆盖层
└── util/
    └── Constants.kt        # 常量
```

## 功能特性

- ✅ 相册多选导入视频（自动拷贝到沙盒）
- ✅ 长按拖拽排序
- ✅ 顺序循环播放（无过渡动画）
- ✅ 声音开关 + 双画幅模式（保持比例 / 拉伸铺满）
- ✅ 全屏锁定 + 三指双击解锁
- ✅ 自动恢复播放（App Kill / 重启 / 切后台）
- ✅ 屏幕常亮
- ✅ 配置持久化（DataStore）
- ✅ 完全离线运行
- ✅ 横屏锁定

## 配置项

| 配置 | 默认值 | 持久化 |
|------|--------|--------|
| 视频列表 | 空 | ✅ |
| 播放顺序 | 导入顺序 | ✅ |
| 声音 | 开 | ✅ |
| 画面填充模式 | 保持比例 | ✅ |
| 当前播放进度 | 0 | ✅（每 5 秒保存） |
