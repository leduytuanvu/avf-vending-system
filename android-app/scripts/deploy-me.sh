#!/usr/bin/env bash
set -euo pipefail

APK_PATH="app/build/outputs/apk/meProductionRelease/app-me-production-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "APK not found. Building ME production release..."
    ./gradlew app:assembleMeProductionRelease
fi

echo "Installing ME APK on connected device..."
adb install -r "$APK_PATH"
echo "Done."
