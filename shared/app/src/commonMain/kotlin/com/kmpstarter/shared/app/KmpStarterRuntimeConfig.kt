package com.kmpstarter.shared.app

enum class KmpStarterEnvironment(
    val id: String,
    val displayName: String,
    val defaultApiBaseUrl: String,
) {
    NonProd(
        id = "nonProd",
        displayName = "NonProd",
        defaultApiBaseUrl = "https://api.nonprod.example.com/",
    ),
    Prod(
        id = "prod",
        displayName = "Prod",
        defaultApiBaseUrl = "https://api.example.com/",
    ),
    ;

    companion object {
        fun fromName(name: String): KmpStarterEnvironment =
            when (name.trim()) {
                Prod.id -> Prod
                else -> NonProd
            }
    }
}

data class KmpStarterRuntimeConfig(
    val environment: KmpStarterEnvironment,
    val apiBaseUrl: String = environment.defaultApiBaseUrl,
    val firebaseWebApiKey: String? = null,
    val googleOAuthClientId: String? = null,
    val googleOAuthRedirectScheme: String? = null,
) {
    companion object {
        val NonProd: KmpStarterRuntimeConfig = KmpStarterRuntimeConfig(KmpStarterEnvironment.NonProd)
        val Prod: KmpStarterRuntimeConfig = KmpStarterRuntimeConfig(KmpStarterEnvironment.Prod)

        fun from(
            environmentName: String,
            apiBaseUrl: String? = null,
            firebaseWebApiKey: String? = null,
            googleOAuthClientId: String? = null,
            googleOAuthRedirectScheme: String? = null,
        ): KmpStarterRuntimeConfig {
            val environment = KmpStarterEnvironment.fromName(environmentName)
            val resolvedBaseUrl = apiBaseUrl?.takeIf { it.isNotBlank() } ?: environment.defaultApiBaseUrl

            return KmpStarterRuntimeConfig(
                environment = environment,
                apiBaseUrl = resolvedBaseUrl,
                firebaseWebApiKey = firebaseWebApiKey.takeIfConfigured(),
                googleOAuthClientId = googleOAuthClientId.takeIfConfigured(),
                googleOAuthRedirectScheme = googleOAuthRedirectScheme.takeIfConfigured(),
            )
        }

        private fun String?.takeIfConfigured(): String? =
            this
                ?.trim()
                ?.takeIf { value ->
                    value.isNotBlank() &&
                        !value.startsWith("$(") &&
                        !value.startsWith("REPLACE_WITH_")
                }
    }
}
