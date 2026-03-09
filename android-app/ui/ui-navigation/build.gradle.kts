plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.avf.vending.ui.navigation"
    compileSdk = 36
    defaultConfig { minSdk = 21 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":ui:ui-theme"))
    implementation(project(":feature:feature-storefront"))
    implementation(project(":feature:feature-payment"))
    implementation(project(":feature:feature-dispensing"))
    implementation(project(":feature:feature-idle"))
    implementation(project(":feature:feature-admin"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}
