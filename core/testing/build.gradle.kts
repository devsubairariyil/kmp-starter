plugins {
    id("kmpstarter.android.library")
}

android {
    namespace = "com.kmpstarter.android.core.testing"
}

dependencies {
    api(project(":core:model"))
    api(libs.coroutines.test)
    api(libs.junit5.api)
    api(libs.turbine)
}
