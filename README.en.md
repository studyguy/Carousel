# Carousel · Exhibition Video Loop App

> An Android tool for **unattended fullscreen video looping** on fixed exhibition booths — showrooms, trade shows, retail stores. Import and play, fullscreen lock against mis-taps, auto-resume after power loss or reboot. Fully offline.

**[📦 Source Code](https://github.com/studyguy/Carousel)**

---

## 📖 What Is This

Promotional screens in showrooms, trade shows, and retail stores need to loop a handful of videos 24/7 — but regular players get exited by stray taps, don't resume after a reboot, and clutter the frame with UI. **Carousel** is built for exactly the "set it and forget it" scenario: multi-select videos from your gallery, play them fullscreen in a loop, unlock with a three-finger double-tap only, and the playback position auto-restores after the app is killed, the device reboots, or you switch away. Deploy it once and stop worrying for months.

Under the hood it's a modern single-Activity Android app built on Jetpack Compose + Media3 ExoPlayer, with config and progress persisted in DataStore — **zero network permissions, fully offline**.

## ✨ Features

- ✅ Multi-select video import from gallery (auto-copied to app sandbox)
- ✅ Long-press drag-to-reorder; sequential looping with no transitions
- ✅ Fullscreen lock + **three-finger double-tap to unlock** (protects against visitor mis-taps)
- ✅ Auto-resume playback (app kill / reboot / background switch; position saved every 5 seconds)
- ✅ Sound toggle + two fill modes (keep aspect ratio / stretch to fill)
- ✅ Screen always on, landscape locked, config persisted (DataStore)
- ✅ Completely offline (no network permission at all)

## 🚀 Quick Start

**Build & run**:

1. Open the project root in Android Studio
2. Wait for Gradle sync to finish
3. Pick a target device and hit Run ▶

**Daily use**: open the app → import videos from the gallery → arrange order and fill mode → go fullscreen; three-finger double-tap the screen to unlock back to the config page.

## ⚙️ Configuration

| Setting | Default | Persisted |
|------|--------|--------|
| Video list | empty | ✅ |
| Play order | import order | ✅ |
| Sound | on | ✅ |
| Fill mode | keep aspect ratio | ✅ |
| Playback position | 0 | ✅ (saved every 5s) |

## 🛠️ Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 (dark theme) |
| Video | Media3 ExoPlayer |
| Persistence | DataStore + Gson |
| Min SDK | Android 10 (API 29) |

## 🧩 Project Structure

```
app/src/main/java/com/carousel/app/
├── CarouselApp.kt          # Application
├── MainActivity.kt         # Single Activity + navigation
├── model/                  # VideoItem / PlayConfig(+FillMode) / UiState
├── data/
│   ├── ConfigRepository.kt # Config persistence (DataStore)
│   └── VideoRepository.kt  # Video file management (sandbox copy)
├── ui/
│   ├── theme/              # Material 3 dark theme
│   ├── home/               # Home (config mode) + ViewModel
│   ├── player/             # Fullscreen player + ViewModel
│   └── components/
│       ├── ThreeFingerDetector.kt   # Three-finger double-tap detection
│       ├── ReorderableLazyColumn.kt # Drag-to-reorder list
│       ├── VideoListItem.kt / SettingsPanel.kt / FillModePreview.kt
│       └── UnlockOverlay.kt         # Unlock overlay
└── util/Constants.kt       # Constants
```

## 📜 Notes

Designed for unattended looping on fixed exhibition booths; Issues welcome for real-world deployment needs.
