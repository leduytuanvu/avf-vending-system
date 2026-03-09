#!/usr/bin/env bash
set -euo pipefail

APK_PATH="app/build/outputs/apk/xyProductionRelease/app-xy-production-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "APK not found. Building XY production release..."
    ./gradlew app:assembleXyProductionRelease
fi

echo "Installing XY APK on connected device..."
adb install -r "$APK_PATH"
echo "Done."
