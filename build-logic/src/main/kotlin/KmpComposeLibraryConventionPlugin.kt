package kmpstarter.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class KmpComposeLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                ((this as ExtensionAware).extensions.getByName("android") as KotlinMultiplatformAndroidLibraryTarget).apply {
                    compileSdk =
                        libs
                            .findVersion("compileSdk")
                            .get()
                            .requiredVersion
                            .toInt()
                    minSdk =
                        libs
                            .findVersion("minSdk")
                            .get()
                            .requiredVersion
                            .toInt()
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_21)
                    }
                }
                jvm("desktop") {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_21)
                    }
                }
                iosX64()
                iosArm64()
                iosSimulatorArm64()
            }

            tasks.withType<KotlinJvmCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
}
