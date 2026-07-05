package com.kmpstarter.shared.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kmpstarter.shared.app.auth.LoginAuthGateway
import com.kmpstarter.shared.app.auth.LoginAuthResult
import com.kmpstarter.shared.app.auth.LoginProvider
import com.kmpstarter.shared.app.auth.availableLoginProviders
import com.kmpstarter.shared.app.auth.currentLoginPlatform
import kotlinx.coroutines.launch

@Composable
internal fun LoginSheet(
    authGateway: LoginAuthGateway,
    onDismiss: () -> Unit,
    onSignedIn: (LoginAuthResult.SignedIn) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val providers = availableLoginProviders(currentLoginPlatform())
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var signingInProvider by remember { mutableStateOf<LoginProvider?>(null) }
    val scrimModifier =
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
    val sheetModifier =
        Modifier
            .fillMaxWidth()
            .heightIn(min = 620.dp)
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
    val contentModifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 28.dp)

    Box(
        modifier = scrimModifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = sheetModifier,
            color = Color.White,
        ) {
            Column(
                modifier = contentModifier,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    val closeModifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .padding(10.dp)

                    Text(
                        text = "Log in or sign up",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "X",
                        modifier = closeModifier,
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Light,
                    )
                }
                Spacer(Modifier.height(74.dp))
                Text(
                    text = "Choose a secure sign-in option to continue.",
                    color = Color(0xFF8F8F94),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(28.dp))
                providers.forEach { provider ->
                    SocialLoginButton(
                        provider = provider,
                        enabled = signingInProvider == null,
                        loading = signingInProvider == provider,
                        onClick = {
                            errorMessage = null
                            signingInProvider = provider
                            coroutineScope.launch {
                                when (val result = authGateway.signIn(provider)) {
                                    LoginAuthResult.Cancelled -> signingInProvider = null
                                    is LoginAuthResult.Failed -> {
                                        errorMessage = result.message
                                        signingInProvider = null
                                    }
                                    is LoginAuthResult.SignedIn -> onSignedIn(result)
                                }
                            }
                        },
                    )
                    Spacer(Modifier.height(14.dp))
                }
                Spacer(Modifier.height(42.dp))
                TermsText()
                errorMessage?.let { message ->
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    provider: LoginProvider,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    val iconColor =
        when (provider) {
            LoginProvider.Facebook -> Color(0xFF1877F2)
            LoginProvider.Google -> Color(0xFF111111)
            LoginProvider.Apple -> Color.Black
        }
    val iconText =
        when (provider) {
            LoginProvider.Apple -> "A"
            LoginProvider.Google -> "G"
            LoginProvider.Facebook -> "f"
        }
    val label =
        if (loading) {
            "Continuing with ${provider.label}"
        } else {
            "Continue with ${provider.label}"
        }
    val buttonModifier =
        Modifier
            .fillMaxWidth()
            .height(62.dp)
            .border(1.5.dp, Color(0xFF414141), RoundedCornerShape(12.dp))
    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledContentColor = Color(0xFF8F8F94),
        )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        colors = buttonColors,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = iconText,
                modifier = Modifier.align(Alignment.CenterStart),
                color = iconColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = label,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TermsText() {
    val linkStyle =
        SpanStyle(
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        )
    val termsText =
        buildAnnotatedString {
            append("By continuing, you agree to KMP Starter's ")
            withStyle(linkStyle) {
                append("Terms of Service")
            }
            append(" and acknowledge the ")
            withStyle(linkStyle) {
                append("Privacy Policy")
            }
            append(".")
        }

    Text(
        text = termsText,
        color = Color.Black,
        style = MaterialTheme.typography.titleMedium,
        lineHeight = MaterialTheme.typography.headlineSmall.lineHeight,
    )
}
