import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("android-library-convention")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }
            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }
            dependencies {
                add("implementation", "com.google.dagger:hilt-android:${Versions.Deps.hilt}")
                add("ksp", "com.google.dagger:hilt-android-compiler:${Versions.Deps.hilt}")
                add("implementation", "androidx.hilt:hilt-navigation-compose:1.2.0")
                add("implementation", "androidx.navigation:navigation-compose:2.8.0")
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
            }
        }
    }
}
