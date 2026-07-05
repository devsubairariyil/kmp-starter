plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
}

android {
    namespace = "com.kmpstarter.android.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coroutines.core)
}
