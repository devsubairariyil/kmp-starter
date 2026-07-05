package kmpstarter.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
            dependencies {
                add("testImplementation", libs.findLibrary("junit5-api").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-platform-launcher").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("coroutines-test").get())
                add("testImplementation", libs.findLibrary("turbine").get())
            }
        }
}
