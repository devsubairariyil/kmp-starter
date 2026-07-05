package com.kmpstarter.android.core.navigation

sealed class KmpStarterRoute(
    val path: String,
) {
    data object Login : KmpStarterRoute("login")

    data object Home : KmpStarterRoute("home")
}
