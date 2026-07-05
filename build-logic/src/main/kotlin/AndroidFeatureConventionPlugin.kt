package kmpstarter.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("kmpstarter.android.library")
            pluginManager.apply("kmpstarter.android.compose")
            pluginManager.apply("kmpstarter.android.hilt")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:design-system"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:navigation"))
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("coroutines-android").get())
            }
        }
}
