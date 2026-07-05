package com.kmpstarter.shared.app

import androidx.compose.ui.window.ComposeUIViewController
import com.kmpstarter.shared.app.auth.LoginAuthGateway
import com.kmpstarter.shared.app.auth.UnavailableLoginAuthGateway

fun MainViewController(
    environmentName: String = KmpStarterEnvironment.NonProd.id,
    apiBaseUrl: String = KmpStarterEnvironment.NonProd.defaultApiBaseUrl,
    firebaseWebApiKey: String = "",
    googleOAuthClientId: String = "",
    googleOAuthRedirectScheme: String = "",
    authGateway: LoginAuthGateway = UnavailableLoginAuthGateway,
) = ComposeUIViewController {
    val runtimeConfig =
        KmpStarterRuntimeConfig.from(
            environmentName = environmentName,
            apiBaseUrl = apiBaseUrl,
            firebaseWebApiKey = firebaseWebApiKey,
            googleOAuthClientId = googleOAuthClientId,
            googleOAuthRedirectScheme = googleOAuthRedirectScheme,
        )

    KmpStarterApp(
        runtimeConfig = runtimeConfig,
        authGateway = authGateway,
    )
}
