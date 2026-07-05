plugins {
    `kotlin-dsl`
}

group = "com.kmpstarter.android.buildlogic"

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "kmpstarter.android.application"
            implementationClass = "kmpstarter.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "kmpstarter.android.library"
            implementationClass = "kmpstarter.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "kmpstarter.android.feature"
            implementationClass = "kmpstarter.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "kmpstarter.android.compose"
            implementationClass = "kmpstarter.buildlogic.AndroidComposeConventionPlugin"
        }
        register("kmpComposeLibrary") {
            id = "kmpstarter.kmp.compose.library"
            implementationClass = "kmpstarter.buildlogic.KmpComposeLibraryConventionPlugin"
        }
        register("desktopComposeApplication") {
            id = "kmpstarter.desktop.compose.application"
            implementationClass = "kmpstarter.buildlogic.DesktopComposeApplicationConventionPlugin"
        }
        register("androidHilt") {
            id = "kmpstarter.android.hilt"
            implementationClass = "kmpstarter.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidTest") {
            id = "kmpstarter.android.test"
            implementationClass = "kmpstarter.buildlogic.AndroidTestConventionPlugin"
        }
    }
}
