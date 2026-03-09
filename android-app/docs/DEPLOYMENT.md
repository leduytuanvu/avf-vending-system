# Deployment Guide

## Build Variants

| Machine | Environment | Type    | Gradle Task                           |
|---------|-------------|---------|---------------------------------------|
| XY      | production  | release | assembleXyProductionRelease           |
| XY      | staging     | debug   | assembleXyStagingDebug                |
| TCN     | production  | release | assembleTcnProductionRelease          |
| TCN     | staging     | debug   | assembleTcnStagingDebug               |
| ME      | production  | release | assembleMeProductionRelease           |
| ME      | staging     | debug   | assembleMeStagingDebug                |
| Mock    | dev         | debug   | assembleMockDevDebug                  |

## Deploy via ADB

```bash
# XY machine
./scripts/deploy-xy.sh

# TCN machine
./scripts/deploy-tcn.sh

# ME machine
./scripts/deploy-me.sh
```

## Sign Release APK

```bash
export KEYSTORE_PATH=/path/to/keystore.jks
export KEYSTORE_PASSWORD=<password>
export KEY_ALIAS=avf-release
export KEY_PASSWORD=<key-password>

# Sign ME flavor (default)
./scripts/sign-release.sh me

# Sign XY flavor
./scripts/sign-release.sh xy
```

## CI/CD (GitHub Actions)

- `pr-check.yml` — runs lint + unit tests on every PR
- `build-xy.yml` — builds XY flavor when XY-related files change
- `build-tcn.yml` — builds TCN flavor when TCN-related files change
- `build-me.yml` — builds ME flavor when ME-related files change
- `build-release.yml` — builds all production APKs on version tags (`v*`)

## Hardware Serial Port Configuration

Connect the Android device via USB. Verify serial port permissions:

```bash
adb shell ls -la /dev/ttyS*
adb shell ls -la /dev/ttyUSB*
```

The app requests `USB_PERMISSION` at runtime on first launch.
