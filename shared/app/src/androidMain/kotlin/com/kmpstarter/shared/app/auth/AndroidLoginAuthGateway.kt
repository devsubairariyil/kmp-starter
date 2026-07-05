package com.kmpstarter.shared.app.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.kmpstarter.shared.app.KmpStarterRuntimeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class AndroidLoginAuthGateway(
    private val activity: Activity,
    private val runtimeConfig: KmpStarterRuntimeConfig,
) : LoginAuthGateway {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingGoogleSignIn: PendingGoogleSignIn? = null

    override suspend fun signIn(provider: LoginProvider): LoginAuthResult =
        when (provider) {
            LoginProvider.Google -> signInWithGoogle()
            LoginProvider.Apple -> LoginAuthResult.Failed("Apple sign-in is available on iOS only.")
            LoginProvider.Facebook -> LoginAuthResult.Failed("Facebook sign-in is not configured yet.")
        }

    fun handleRedirect(uri: Uri?) {
        val pending = pendingGoogleSignIn ?: return
        if (uri?.scheme != pending.redirectScheme) return

        pendingGoogleSignIn = null
        val error = uri.getQueryParameter("error")
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")

        when {
            error != null -> pending.continuation.resume(LoginAuthResult.Failed(error))
            state != pending.state -> pending.continuation.resume(LoginAuthResult.Failed("Google sign-in state did not match."))
            code.isNullOrBlank() -> pending.continuation.resume(LoginAuthResult.Cancelled)
            else -> {
                ioScope.launch {
                    pending.continuation.resume(exchangeGoogleCode(code, pending.codeVerifier, pending.redirectUri))
                }
            }
        }
    }

    private suspend fun signInWithGoogle(): LoginAuthResult {
        val firebaseApiKey = runtimeConfig.firebaseWebApiKey
        val googleClientId = runtimeConfig.googleOAuthClientId
        val redirectScheme = runtimeConfig.googleOAuthRedirectScheme
        if (firebaseApiKey.isNullOrBlank() || googleClientId.isNullOrBlank() || redirectScheme.isNullOrBlank()) {
            return LoginAuthResult.Failed("Google sign-in is missing Firebase or Google OAuth configuration.")
        }

        val state = randomUrlSafeString()
        val codeVerifier = randomUrlSafeString(byteCount = 64)
        val redirectUri = "$redirectScheme:/oauth2redirect"
        val authUri =
            Uri
                .parse("https://accounts.google.com/o/oauth2/v2/auth")
                .buildUpon()
                .appendQueryParameter("client_id", googleClientId)
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", "openid email profile")
                .appendQueryParameter("state", state)
                .appendQueryParameter("code_challenge", sha256Base64Url(codeVerifier))
                .appendQueryParameter("code_challenge_method", "S256")
                .build()

        return suspendCancellableCoroutine { continuation ->
            pendingGoogleSignIn =
                PendingGoogleSignIn(
                    state = state,
                    codeVerifier = codeVerifier,
                    redirectUri = redirectUri,
                    redirectScheme = redirectScheme,
                    continuation = continuation,
                )
            continuation.invokeOnCancellation {
                if (pendingGoogleSignIn?.state == state) pendingGoogleSignIn = null
            }
            activity.startActivity(Intent(Intent.ACTION_VIEW, authUri))
        }
    }

    private fun exchangeGoogleCode(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): LoginAuthResult =
        runCatching {
            val tokenRequestValues =
                mapOf(
                    "client_id" to requireNotNull(runtimeConfig.googleOAuthClientId),
                    "code" to code,
                    "code_verifier" to codeVerifier,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to redirectUri,
                )
            val tokenJson =
                postForm(
                    url = "https://oauth2.googleapis.com/token",
                    values = tokenRequestValues,
                )
            val idToken = JSONObject(tokenJson).getString("id_token")
            val firebaseRequestBody =
                JSONObject()
                    .put("postBody", "id_token=${urlEncode(idToken)}&providerId=google.com")
                    .put("requestUri", "http://localhost")
                    .put("returnIdpCredential", true)
                    .put("returnSecureToken", true)
                    .toString()
            val firebaseJson =
                postJson(
                    url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=${runtimeConfig.firebaseWebApiKey}",
                    body = firebaseRequestBody,
                )
            val firebaseUser = JSONObject(firebaseJson)
            LoginAuthResult.SignedIn(
                userId = firebaseUser.getString("localId"),
                displayName = firebaseUser.optString("displayName").takeIf { it.isNotBlank() },
                email = firebaseUser.optString("email").takeIf { it.isNotBlank() },
                idToken = firebaseUser.optString("idToken").takeIf { it.isNotBlank() },
            )
        }.getOrElse { throwable ->
            LoginAuthResult.Failed("Google sign-in failed.", throwable)
        }

    private fun postForm(
        url: String,
        values: Map<String, String>,
    ): String =
        post(
            url = url,
            contentType = "application/x-www-form-urlencoded",
            body = values.entries.joinToString("&") { "${urlEncode(it.key)}=${urlEncode(it.value)}" },
        )

    private fun postJson(
        url: String,
        body: String,
    ): String = post(url = url, contentType = "application/json", body = body)

    private fun post(
        url: String,
        contentType: String,
        body: String,
    ): String {
        val connection =
            (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", contentType)
            }
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body)
        }
        val responseCode = connection.responseCode
        val responseBody =
            if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
            }
        if (responseCode !in 200..299) error(responseBody.ifBlank { "HTTP $responseCode" })
        return responseBody
    }

    private fun randomUrlSafeString(byteCount: Int = 32): String {
        val bytes = ByteArray(byteCount)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun sha256Base64Url(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private data class PendingGoogleSignIn(
        val state: String,
        val codeVerifier: String,
        val redirectUri: String,
        val redirectScheme: String,
        val continuation: Continuation<LoginAuthResult>,
    )
}
