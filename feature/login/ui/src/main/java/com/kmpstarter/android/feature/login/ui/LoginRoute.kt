package com.kmpstarter.android.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmpstarter.android.core.designsystem.KmpStarterPrimaryButton
import com.kmpstarter.android.core.designsystem.KmpStarterSpacing

@Composable
fun LoginRoute(
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthenticated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(KmpStarterSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("KMP Starter", style = MaterialTheme.typography.headlineLarge)
        Text("Shared Kotlin Multiplatform foundation", style = MaterialTheme.typography.bodyLarge)
        KmpStarterPrimaryButton(
            text = "Continue",
            onClick = viewModel::continueAsGuest,
        )
    }
}
