import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }
            dependencies {
                add("implementation", "com.google.dagger:hilt-android:${Versions.Deps.hilt}")
                add("ksp", "com.google.dagger:hilt-android-compiler:${Versions.Deps.hilt}")
            }
        }
    }
}
