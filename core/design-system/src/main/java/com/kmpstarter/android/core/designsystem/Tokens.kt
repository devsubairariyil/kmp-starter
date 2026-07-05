package com.kmpstarter.android.core.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val KmpStarterTypography = Typography()
val KmpStarterShapes = Shapes()

object KmpStarterSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

@Composable
fun KmpStarterPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = KmpStarterSpacing.lg, vertical = KmpStarterSpacing.sm),
    ) {
        Text(text)
    }
}

@Composable
fun KmpStarterTextButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun KmpStarterLoading() {
    CircularProgressIndicator()
}
