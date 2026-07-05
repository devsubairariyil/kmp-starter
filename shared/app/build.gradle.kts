plugins {
    id("kmpstarter.kmp.compose.library")
}

kotlin {
    android {
        namespace = "com.kmpstarter.shared.app"
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "KmpStarterShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.coroutines.core)
        }
        iosMain.dependencies {
            implementation(compose.ui)
        }
    }
}
