pluginManagement {
    includeBuild("build-src")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "android-app"

include(":app")

// Core
include(":core:core-domain")
include(":core:core-common")
include(":core:core-testing")
include(":core:core-logger")

// Data
include(":data:data-local")
include(":data:data-remote")
include(":data:data-repository")

// Hardware
include(":hardware:hardware-api")
include(":hardware:hardware-transport")
include(":hardware:hardware-xy")
include(":hardware:hardware-tcn")
include(":hardware:hardware-co")
include(":hardware:hardware-bill")
include(":hardware:hardware-mock")

// Payment
include(":payment")

// Sync
include(":sync")

// Config
include(":config")

// Features
include(":feature:feature-storefront")
include(":feature:feature-payment")
include(":feature:feature-dispensing")
include(":feature:feature-idle")
include(":feature:feature-admin")

// UI
include(":ui:ui-theme")
include(":ui:ui-components")
include(":ui:ui-navigation")
