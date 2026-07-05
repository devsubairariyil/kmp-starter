plugins {
    id("kmpstarter.android.feature")
}

android {
    namespace = "com.kmpstarter.android.feature.home.ui"
}

dependencies {
    implementation(project(":feature:home:domain"))
}
