package com.kmpstarter.shared.app.auth

import com.kmpstarter.shared.app.KmpStarterRuntimeConfig
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class DesktopLoginAuthGateway(
    private val runtimeConfig: KmpStarterRuntimeConfig,
) : LoginAuthGateway {
    private val httpClient = HttpClient.newHttpClient()

    override suspend fun signIn(provider: LoginProvider): LoginAuthResult =
        when (provider) {
            LoginProvider.Google -> signInWithGoogle()
            LoginProvider.Apple -> LoginAuthResult.Failed("Apple sign-in is available on iOS only.")
            LoginProvider.Facebook -> LoginAuthResult.Failed("Facebook sign-in is not configured yet.")
        }

    private suspend fun signInWithGoogle(): LoginAuthResult =
        withContext(Dispatchers.IO) {
            val firebaseApiKey = runtimeConfig.firebaseWebApiKey
            val googleClientId = runtimeConfig.googleOAuthClientId
            if (firebaseApiKey.isNullOrBlank() || googleClientId.isNullOrBlank()) {
                return@withContext LoginAuthResult.Failed("Google sign-in is missing Firebase or Google OAuth configuration.")
            }

            val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            val redirectUri = "http://127.0.0.1:${server.address.port}/oauth2redirect"
            val state = randomUrlSafeString()
            val codeVerifier = randomUrlSafeString(byteCount = 64)
            val authResult = CompletableDeferred<LoginAuthResult>()

            server.createContext("/oauth2redirect") { exchange ->
                val query = parseQuery(exchange.requestURI.rawQuery.orEmpty())
                val response = "You can return to KmpStarter."
                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { it.write(response.toByteArray()) }

                when {
                    query["error"] != null -> authResult.complete(LoginAuthResult.Failed(query.getValue("error")))
                    query["state"] != state -> authResult.complete(LoginAuthResult.Failed("Google sign-in state did not match."))
                    query["code"].isNullOrBlank() -> authResult.complete(LoginAuthResult.Cancelled)
                    else -> {
                        val result = exchangeGoogleCode(query.getValue("code"), codeVerifier, redirectUri)
                        authResult.complete(result)
                    }
                }
            }
            server.start()

            val authUri =
                URI(
                    "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=${urlEncode(googleClientId)}" +
                        "&redirect_uri=${urlEncode(redirectUri)}" +
                        "&response_type=code" +
                        "&scope=${urlEncode("openid email profile")}" +
                        "&state=${urlEncode(state)}" +
                        "&code_challenge=${urlEncode(sha256Base64Url(codeVerifier))}" +
                        "&code_challenge_method=S256",
                )
            Desktop.getDesktop().browse(authUri)

            try {
                authResult.await()
            } finally {
                server.stop(0)
            }
        }

    private fun exchangeGoogleCode(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): LoginAuthResult =
        runCatching {
            val tokenRequestBody =
                mapOf(
                    "client_id" to requireNotNull(runtimeConfig.googleOAuthClientId),
                    "code" to code,
                    "code_verifier" to codeVerifier,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to redirectUri,
                ).entries.joinToString("&") { "${urlEncode(it.key)}=${urlEncode(it.value)}" }
            val tokenJson =
                post(
                    url = "https://oauth2.googleapis.com/token",
                    contentType = "application/x-www-form-urlencoded",
                    body = tokenRequestBody,
                )
            val googleIdToken = requireJsonString(tokenJson, "id_token")
            val firebaseRequestBody =
                """
                {
                  "postBody":"id_token=${urlEncode(googleIdToken)}&providerId=google.com",
                  "requestUri":"http://localhost",
                  "returnIdpCredential":true,
                  "returnSecureToken":true
                }
                """.trimIndent()
            val firebaseJson =
                post(
                    url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=${runtimeConfig.firebaseWebApiKey}",
                    contentType = "application/json",
                    body = firebaseRequestBody,
                )
            LoginAuthResult.SignedIn(
                userId = requireJsonString(firebaseJson, "localId"),
                displayName = optionalJsonString(firebaseJson, "displayName"),
                email = optionalJsonString(firebaseJson, "email"),
                idToken = optionalJsonString(firebaseJson, "idToken"),
            )
        }.getOrElse { throwable ->
            LoginAuthResult.Failed("Google sign-in failed.", throwable)
        }

    private fun post(
        url: String,
        contentType: String,
        body: String,
    ): String {
        val request =
            HttpRequest
                .newBuilder(URI(url))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) error(response.body().ifBlank { "HTTP ${response.statusCode()}" })
        return response.body()
    }

    private fun parseQuery(query: String): Map<String, String> =
        query
            .split("&")
            .filter { it.contains("=") }
            .associate {
                val key = it.substringBefore("=")
                val value = it.substringAfter("=")
                key to java.net.URLDecoder.decode(value, Charsets.UTF_8.name())
            }

    private fun requireJsonString(
        json: String,
        key: String,
    ): String = requireNotNull(optionalJsonString(json, key)) { "Missing JSON field: $key" }

    private fun optionalJsonString(
        json: String,
        key: String,
    ): String? =
        Regex(""""$key"\s*:\s*"([^"]*)"""")
            .find(json)
            ?.groupValues
            ?.get(1)
            ?.takeIf { it.isNotBlank() }

    private fun randomUrlSafeString(byteCount: Int = 32): String {
        val bytes = ByteArray(byteCount)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256Base64Url(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
