plugins {
    id("kmpstarter.android.library")
}

android {
    namespace = "com.kmpstarter.android.feature.home.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.coroutines.core)
}
