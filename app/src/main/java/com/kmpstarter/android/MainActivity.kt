package com.kmpstarter.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kmpstarter.shared.app.KmpStarterApp
import com.kmpstarter.shared.app.KmpStarterRuntimeConfig
import com.kmpstarter.shared.app.auth.AndroidLoginAuthGateway
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var loginAuthGateway: AndroidLoginAuthGateway

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val runtimeConfig =
            KmpStarterRuntimeConfig.from(
                environmentName = BuildConfig.ENVIRONMENT,
                apiBaseUrl = BuildConfig.API_BASE_URL,
                firebaseWebApiKey = BuildConfig.FIREBASE_WEB_API_KEY,
                googleOAuthClientId = BuildConfig.GOOGLE_OAUTH_CLIENT_ID,
                googleOAuthRedirectScheme = BuildConfig.GOOGLE_OAUTH_REDIRECT_SCHEME,
            )
        loginAuthGateway = AndroidLoginAuthGateway(this, runtimeConfig)
        setContent {
            KmpStarterApp(
                runtimeConfig = runtimeConfig,
                authGateway = loginAuthGateway,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loginAuthGateway.handleRedirect(intent.data)
    }
}
