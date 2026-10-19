# Carousel · 展厅轮播 App

> 展厅 / 展会 / 门店固定展位的**无人值守全屏视频循环轮播** Android 工具 —— 导入即播、全屏锁定防误触、断电重启自动续播，完全离线运行。

**[📦 源码仓库](https://github.com/studyguy/Carousel)**

---

## 📖 这是什么

展厅、展会、门店的宣传屏需要 7×24 小时循环播放几段视频，但普通播放器会被误触退出、重启后不续播、界面元素干扰画面。**Carousel** 就是为这种「摆上去就别管」的场景做的：从相册多选导入视频后全屏循环播放，三指双击才能解锁退出，App 被杀 / 设备重启 / 切后台后自动恢复播放进度——部署一次，安心半年。

技术上是单 Activity + Jetpack Compose + Media3 ExoPlayer 的现代 Android 应用，配置与进度持久化在 DataStore，**零网络权限、完全离线**。

## ✨ 核心功能

- ✅ 相册多选导入视频（自动拷贝到沙盒）
- ✅ 长按拖拽排序，顺序循环播放（无过渡动画）
- ✅ 全屏锁定 + **三指双击解锁**（防展厅观众误触）
- ✅ 自动恢复播放（App Kill / 重启 / 切后台，进度每 5 秒保存）
- ✅ 声音开关 + 双画幅模式（保持比例 / 拉伸铺满）
- ✅ 屏幕常亮、横屏锁定、配置持久化（DataStore）
- ✅ 完全离线运行（无任何网络权限）

## 🚀 快速开始

**构建与运行**：

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择目标设备，点击 Run ▶

**日常使用**：打开 App → 从相册导入视频 → 调整顺序与画幅 → 全屏播放；三指双击屏幕解锁回到配置页。

## ⚙️ 配置项

| 配置 | 默认值 | 持久化 |
|------|--------|--------|
| 视频列表 | 空 | ✅ |
| 播放顺序 | 导入顺序 | ✅ |
| 声音 | 开 | ✅ |
| 画面填充模式 | 保持比例 | ✅ |
| 当前播放进度 | 0 | ✅（每 5 秒保存） |

## 🛠️ 技术栈

| 层 | 选型 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3（暗色主题） |
| 视频 | Media3 ExoPlayer |
| 持久化 | DataStore + Gson |
| 最低版本 | Android 10（API 29） |

## 🧩 项目结构

```
app/src/main/java/com/carousel/app/
├── CarouselApp.kt          # Application
├── MainActivity.kt         # 单 Activity + 导航
├── model/                  # VideoItem / PlayConfig(+FillMode) / UiState
├── data/
│   ├── ConfigRepository.kt # 配置持久化（DataStore）
│   └── VideoRepository.kt  # 视频文件管理（沙盒拷贝）
├── ui/
│   ├── theme/              # Material 3 暗色主题
│   ├── home/               # 首页（配置模式）+ ViewModel
│   ├── player/             # 全屏播放 + ViewModel
│   └── components/
│       ├── ThreeFingerDetector.kt   # 三指双击检测
│       ├── ReorderableLazyColumn.kt # 拖拽排序列表
│       ├── VideoListItem.kt / SettingsPanel.kt / FillModePreview.kt
│       └── UnlockOverlay.kt         # 解锁覆盖层
└── util/Constants.kt       # 常量
```

## 📜 声明

为固定展位无人值守循环播放场景设计；欢迎按实际部署需求提出 Issue。
