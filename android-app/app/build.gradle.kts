plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.avf.vending"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.avf.vending"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += listOf("machine", "environment")

    productFlavors {
        // Machine flavors
        create("xy") {
            dimension = "machine"
            applicationIdSuffix = ".xy"
            versionNameSuffix = "-xy"
        }
        create("tcn") {
            dimension = "machine"
            applicationIdSuffix = ".tcn"
            versionNameSuffix = "-tcn"
        }
        create("co") {
            dimension = "machine"
            applicationIdSuffix = ".co"
            versionNameSuffix = "-co"
        }
        create("mock") {
            dimension = "machine"
            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"
        }

        // Environment flavors
        create("production") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.avf.vending/v1/\"")
            buildConfigField("String", "SENTRY_DSN", "\"\"")
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://staging.api.avf.vending/v1/\"")
            buildConfigField("String", "SENTRY_DSN", "\"\"")
        }
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://dev.api.avf.vending/v1/\"")
            buildConfigField("String", "SENTRY_DSN", "\"\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "../proguard-rules-base.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logger"))

    // Data
    implementation(project(":data:data-local"))
    implementation(project(":data:data-remote"))
    implementation(project(":data:data-repository"))

    // Hardware
    implementation(project(":hardware:hardware-api"))
    implementation(project(":hardware:hardware-transport"))
    implementation(project(":hardware:hardware-bill"))
    "xyImplementation"(project(":hardware:hardware-xy"))
    "tcnImplementation"(project(":hardware:hardware-tcn"))
    "coImplementation"(project(":hardware:hardware-co"))
    "mockImplementation"(project(":hardware:hardware-mock"))

    // Payment
    implementation(project(":payment"))

    // Sync + Config
    implementation(project(":sync"))
    implementation(project(":config"))

    // Features
    implementation(project(":feature:feature-storefront"))
    implementation(project(":feature:feature-payment"))
    implementation(project(":feature:feature-dispensing"))
    implementation(project(":feature:feature-idle"))
    implementation(project(":feature:feature-admin"))

    // UI
    implementation(project(":ui:ui-theme"))
    implementation(project(":ui:ui-components"))
    implementation(project(":ui:ui-navigation"))

    // AndroidX + Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Timber
    implementation(libs.timber)

    // Sentry crash reporting
    implementation(libs.sentry.android)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
