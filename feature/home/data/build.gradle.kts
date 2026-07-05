plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
}

android {
    namespace = "com.kmpstarter.android.feature.home.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":feature:home:domain"))
}
