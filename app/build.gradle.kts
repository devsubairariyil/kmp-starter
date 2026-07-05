plugins {
    id("kmpstarter.android.application")
    id("kmpstarter.android.compose")
    id("kmpstarter.android.hilt")
    id("kmpstarter.android.test")
}

dependencies {
    implementation(project(":shared:app"))
    implementation(project(":core:design-system"))
    implementation(project(":core:navigation"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:login:data"))
    implementation(project(":feature:login:ui"))
    implementation(project(":feature:home:data"))
    implementation(project(":feature:home:ui"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    implementation(libs.timber)
}
