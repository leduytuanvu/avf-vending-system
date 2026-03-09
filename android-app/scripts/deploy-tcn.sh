#!/usr/bin/env bash
set -euo pipefail

APK_PATH="app/build/outputs/apk/tcnProductionRelease/app-tcn-production-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "APK not found. Building TCN production release..."
    ./gradlew app:assembleTcnProductionRelease
fi

echo "Installing TCN APK on connected device..."
adb install -r "$APK_PATH"
echo "Done."
