package com.kmpstarter.shared.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kmpstarter.shared.app.auth.LoginAuthGateway
import com.kmpstarter.shared.app.auth.UnavailableLoginAuthGateway

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF006A60),
        secondary = Color(0xFF6B5E00),
        tertiary = Color(0xFF8B3F47),
        background = Color(0xFFFBFCF8),
        surface = Color(0xFFFFFFFF),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF62DCCE),
        secondary = Color(0xFFE1C95A),
        tertiary = Color(0xFFFFB3BC),
        background = Color(0xFF111412),
        surface = Color(0xFF191C1A),
    )

@Composable
fun KmpStarterApp(
    runtimeConfig: KmpStarterRuntimeConfig = KmpStarterRuntimeConfig.NonProd,
    authGateway: LoginAuthGateway = UnavailableLoginAuthGateway,
) {
    KmpStarterTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            WelcomeScreen(
                runtimeConfig = runtimeConfig,
                authGateway = authGateway,
            )
        }
    }
}

@Composable
fun KmpStarterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

@Composable
fun WelcomeScreen(
    runtimeConfig: KmpStarterRuntimeConfig,
    authGateway: LoginAuthGateway,
    modifier: Modifier = Modifier,
) {
    var selectedMode by remember { mutableStateOf("Guest") }
    var showLoginSheet by remember { mutableStateOf(false) }
    var signedInName by remember { mutableStateOf<String?>(null) }
    val screenModifier =
        modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 32.dp)

    LaunchedEffect(Unit) {
        showLoginSheet = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = screenModifier,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandMark()
            Spacer(Modifier.height(28.dp))
            Text(
                text = "KMP Starter",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "A Kotlin Multiplatform starter ready for Android, iOS, and desktop.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Environment: ${runtimeConfig.environment.displayName}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(28.dp))
            PlatformStatusRow()
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WelcomeModeButton(
                    text = "Guest",
                    selected = selectedMode == "Guest",
                    onClick = { selectedMode = "Guest" },
                    modifier = Modifier.weight(1f),
                )
                WelcomeModeButton(
                    text = "Log in",
                    selected = selectedMode == "Member",
                    onClick = {
                        selectedMode = "Member"
                        showLoginSheet = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = signedInName?.let { "Signed in: $it" } ?: "Selected: $selectedMode",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f),
            )
        }

        if (showLoginSheet) {
            LoginSheet(
                authGateway = authGateway,
                onDismiss = { showLoginSheet = false },
                onSignedIn = { result ->
                    signedInName = result.displayName ?: result.email ?: "Member"
                    selectedMode = "Member"
                    showLoginSheet = false
                },
            )
        }
    }
}

@Composable
private fun BrandMark() {
    val markModifier =
        Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary)

    Box(
        modifier = markModifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "KMP",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PlatformStatusRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PlatformStatus("Android")
        PlatformStatus("iOS")
        PlatformStatus("Desktop")
    }
}

@Composable
private fun PlatformStatus(text: String) {
    val containerModifier =
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    val dotModifier =
        Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = dotModifier,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WelcomeModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
    ) {
        Text(text)
    }
}
