package kmpstarter.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            pluginManager.withPlugin("com.android.application") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
            pluginManager.withPlugin("com.android.library") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }
        }
}
