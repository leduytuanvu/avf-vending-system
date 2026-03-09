#!/usr/bin/env bash
set -euo pipefail

APK_PATH="app/build/outputs/apk/coProductionRelease/app-co-production-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "APK not found. Building CO production release..."
    ./gradlew app:assembleCoProductionRelease
fi

echo "Installing CO APK on connected device..."
adb install -r "$APK_PATH"
echo "Done."
