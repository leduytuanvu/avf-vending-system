plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // compileOnly: provides APIs for compilation but not exported to parent build's classpath,
    // avoiding version conflicts with the plugin versions declared in the main build's version catalog.
    compileOnly("com.android.tools.build:gradle:9.0.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "android-library-convention"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("androidFeature") {
            id = "android-feature-convention"
            implementationClass = "AndroidFeaturePlugin"
        }
        register("androidHilt") {
            id = "android-hilt-convention"
            implementationClass = "AndroidHiltPlugin"
        }
        register("kotlinLibrary") {
            id = "kotlin-library-convention"
            implementationClass = "KotlinLibraryPlugin"
        }
    }
}
