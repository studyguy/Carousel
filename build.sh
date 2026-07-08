#!/bin/bash
# ============================================================
# 展厅轮播 App — 构建 & 安装脚本
# 用法: ./build.sh          → 仅构建 APK
#       ./build.sh install  → 构建并 adb 安装到已连接设备
# ============================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null)}"

APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
ADB="$ANDROID_HOME/platform-tools/adb"

echo "========================================="
echo "  展厅轮播 App 构建"
echo "  ANDROID_HOME=$ANDROID_HOME"
echo "  JAVA_HOME=$JAVA_HOME"
echo "========================================="

cd "$PROJECT_DIR"

# ---- 构建 ----
echo ""
echo "🔨 开始构建..."
./gradlew assembleDebug
echo ""
echo "✅ 构建完成"

if [ -f "$APK" ]; then
    SIZE=$(ls -lh "$APK" | awk '{print $5}')
    echo "📦 APK: $APK ($SIZE)"
else
    echo "❌ APK 未找到！"
    exit 1
fi

# ---- 安装 ----
if [ "${1:-}" = "install" ]; then
    echo ""
    echo "📱 检查设备..."
    DEVICES=$("$ADB" devices 2>/dev/null | tail -n +2 | grep -v '^$' | wc -l | tr -d ' ')
    if [ "$DEVICES" -eq 0 ]; then
        echo "⚠️  未检测到已连接设备"
        echo ""
        echo "📋 手动安装指令："
        echo "   $ADB install -r \"$APK\""
        exit 1
    fi
    echo "🔌 正在安装到设备..."
    "$ADB" install -r "$APK"
    echo ""
    echo "✅ 安装完成！"
else
    echo ""
    echo "📋 adb 安装指令："
    echo "   $ADB install -r \"$APK\""
    echo ""
    echo "💡 连接手机后运行: ./build.sh install"
fi
