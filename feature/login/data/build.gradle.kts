plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
}

android {
    namespace = "com.kmpstarter.android.feature.login.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":feature:login:domain"))
    implementation(libs.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coroutines.core)
}
