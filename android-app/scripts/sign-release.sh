#!/usr/bin/env bash
set -euo pipefail

# Required env vars: KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
: "${KEYSTORE_PATH:?KEYSTORE_PATH is required}"
: "${KEYSTORE_PASSWORD:?KEYSTORE_PASSWORD is required}"
: "${KEY_ALIAS:?KEY_ALIAS is required}"
: "${KEY_PASSWORD:?KEY_PASSWORD is required}"

FLAVOR="${1:-me}"
APK_DIR="app/build/outputs/apk/${FLAVOR}ProductionRelease"
APK_IN="${APK_DIR}/app-${FLAVOR}-production-release-unsigned.apk"
APK_OUT="${APK_DIR}/app-${FLAVOR}-production-release-signed.apk"

if [ ! -f "$APK_IN" ]; then
    echo "Unsigned APK not found: $APK_IN"
    exit 1
fi

echo "Signing APK for flavor: $FLAVOR"
apksigner sign \
    --ks "$KEYSTORE_PATH" \
    --ks-pass "pass:$KEYSTORE_PASSWORD" \
    --ks-key-alias "$KEY_ALIAS" \
    --key-pass "pass:$KEY_PASSWORD" \
    --out "$APK_OUT" \
    "$APK_IN"

echo "Signed APK: $APK_OUT"
apksigner verify "$APK_OUT"
echo "Verification passed."
