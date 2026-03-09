# AVF Vending System — Android App

Enterprise vending machine Android application using Clean Architecture + MVI + Strategy Pattern.

## Module Graph

```
:app (shell — 4 machine flavors × 3 env flavors = 24 variants)
 ├── :feature:feature-storefront
 ├── :feature:feature-payment
 ├── :feature:feature-dispensing
 ├── :feature:feature-idle
 ├── :feature:feature-admin
 ├── :ui:ui-navigation
 ├── :ui:ui-components
 ├── :ui:ui-theme
 ├── :payment
 ├── :sync
 ├── :config
 ├── :hardware:hardware-xy / hardware-tcn / hardware-me / hardware-mock
 ├── :hardware:hardware-bill
 ├── :hardware:hardware-transport
 ├── :hardware:hardware-api
 ├── :data:data-repository
 ├── :data:data-remote
 ├── :data:data-local
 ├── :core:core-logger
 ├── :core:core-common
 └── :core:core-domain  (pure Kotlin, no Android)
```

## Machine Flavors

| Flavor | Application ID        | Protocol        | Strategy Chain                    |
|--------|-----------------------|-----------------|-----------------------------------|
| xy     | com.avf.vending.xy    | XY Serial 9600N | Serial S0 → Serial S1 → USB      |
| tcn    | com.avf.vending.tcn   | TCN 19200N RS485| RS485 → TCP/IP → Serial backup   |
| me     | com.avf.vending.me    | Custom Serial   | Serial S0 → Serial S2 → USB      |
| mock   | com.avf.vending.mock  | In-memory       | —                                 |

## Build Variants

`4 machines × 3 envs (production/staging/dev) × 2 types (debug/release) = 24 variants`

## Key Libraries

- Hilt 2.52 — dependency injection
- Room 2.6.1 — local database (WAL mode)
- Retrofit 2.11.0 + OkHttp 4.12.0 — network
- DataStore 1.1.1 — preferences & config
- Kotlin Coroutines 1.9.0 + Flow
- Jetpack Compose BOM 2024.09.00
- Navigation Compose 2.8.0

## Bill Validator

ICT-BC protocol: `[DA][LNG][SA][RC][CMD][DATA][FCC=XOR]` — 9600 baud, 8E1 (Even parity required).

## Build

```bash
# All production APKs
./gradlew assembleProductionRelease

# Specific flavor
./gradlew app:assembleMeProductionRelease

# Unit tests
./gradlew :core:core-domain:test

# Lint
./gradlew lint
```
