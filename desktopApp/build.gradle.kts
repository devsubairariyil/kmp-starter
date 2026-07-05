plugins {
    id("kmpstarter.desktop.compose.application")
}

dependencies {
    implementation(project(":shared:app"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.kmpstarter.desktop.MainKt"
    }
}
