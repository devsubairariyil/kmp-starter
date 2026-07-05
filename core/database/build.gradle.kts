plugins {
    id("kmpstarter.android.library")
    id("kmpstarter.android.hilt")
    alias(libs.plugins.room)
}

android {
    namespace = "com.kmpstarter.android.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
