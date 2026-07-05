package com.kmpstarter.shared.app.auth

enum class LoginPlatform {
    Android,
    Ios,
    Desktop,
}

expect fun currentLoginPlatform(): LoginPlatform

fun availableLoginProviders(platform: LoginPlatform): List<LoginProvider> =
    when (platform) {
        LoginPlatform.Ios -> listOf(LoginProvider.Apple, LoginProvider.Google, LoginProvider.Facebook)
        LoginPlatform.Android,
        LoginPlatform.Desktop,
        -> listOf(LoginProvider.Google, LoginProvider.Facebook)
    }
