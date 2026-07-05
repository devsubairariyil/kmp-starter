plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
    id("kmpstarter.android.test")
}

android {
    namespace = "com.kmpstarter.android.feature.login.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.coroutines.core)
}
