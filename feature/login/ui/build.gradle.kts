plugins {
    id("kmpstarter.android.feature")
}

android {
    namespace = "com.kmpstarter.android.feature.login.ui"
}

dependencies {
    implementation(project(":feature:login:domain"))
}
