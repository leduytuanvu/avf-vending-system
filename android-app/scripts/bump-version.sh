#!/usr/bin/env bash
set -euo pipefail

GRADLE_FILE="app/build.gradle.kts"

CURRENT=$(grep 'versionCode' "$GRADLE_FILE" | grep -o '[0-9]*')
NEXT=$((CURRENT + 1))

sed -i "s/versionCode = $CURRENT/versionCode = $NEXT/" "$GRADLE_FILE"

echo "Bumped versionCode: $CURRENT -> $NEXT"
