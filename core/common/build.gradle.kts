plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
    id("kmpstarter.android.test")
}

android {
    namespace = "com.kmpstarter.android.core.common"
}

dependencies {
    implementation(libs.coroutines.core)
}
