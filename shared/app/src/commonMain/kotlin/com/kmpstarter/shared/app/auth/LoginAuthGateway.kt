package com.kmpstarter.shared.app.auth

interface LoginAuthGateway {
    suspend fun signIn(provider: LoginProvider): LoginAuthResult
}

enum class LoginProvider(
    val label: String,
) {
    Apple("Apple"),
    Google("Google"),
    Facebook("Facebook"),
}

sealed interface LoginAuthResult {
    data class SignedIn(
        val userId: String,
        val displayName: String?,
        val email: String?,
        val idToken: String?,
    ) : LoginAuthResult

    data object Cancelled : LoginAuthResult

    data class Failed(
        val message: String,
        val cause: Throwable? = null,
    ) : LoginAuthResult
}

object UnavailableLoginAuthGateway : LoginAuthGateway {
    override suspend fun signIn(provider: LoginProvider): LoginAuthResult =
        LoginAuthResult.Failed("${provider.label} sign-in is not configured for this build.")
}
