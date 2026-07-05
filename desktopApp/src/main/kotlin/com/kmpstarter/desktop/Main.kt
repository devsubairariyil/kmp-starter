package com.kmpstarter.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kmpstarter.shared.app.KmpStarterApp
import com.kmpstarter.shared.app.KmpStarterRuntimeConfig
import com.kmpstarter.shared.app.auth.DesktopLoginAuthGateway
import java.io.File
import java.util.Properties

fun main() {
    val runtimeConfig =
        KmpStarterRuntimeConfig.from(
            environmentName = configValue("KMP_STARTER_ENVIRONMENT", "nonProd"),
            apiBaseUrl = configValue("KMP_STARTER_API_BASE_URL", "https://api.nonprod.example.com/"),
            firebaseWebApiKey = configValue("FIREBASE_WEB_API_KEY", ""),
            googleOAuthClientId = configValue("GOOGLE_DESKTOP_CLIENT_ID", configValue("GOOGLE_OAUTH_CLIENT_ID", "")),
        )
    val authGateway = DesktopLoginAuthGateway(runtimeConfig)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Starter",
        ) {
            KmpStarterApp(
                runtimeConfig = runtimeConfig,
                authGateway = authGateway,
            )
        }
    }
}

private fun configValue(
    key: String,
    fallback: String,
): String =
    System.getProperty(key)
        ?: System.getenv(key)
        ?: localProperties().getProperty(key)
        ?: fallback

private fun localProperties(): Properties {
    val properties = Properties()
    val file = File("local.properties")
    if (file.isFile) {
        file.inputStream().use(properties::load)
    }
    return properties
}
