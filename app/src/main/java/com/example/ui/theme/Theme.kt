package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SophisticatedColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = AccentPurpleOnContainer,
    primaryContainer = AccentPurpleContainer,
    onPrimaryContainer = AccentPurpleOnContainer,
    secondary = PurpleGrey80,
    background = DarkBG,
    onBackground = TextLight,
    surface = DarkBG,
    onSurface = TextLight,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSub,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode as requested by "Sophisticated Dark" theme
    content: @Composable () -> Unit,
) {
    // We strictly use the Sophisticated Dark theme scheme as user requested this specific design theme
    MaterialTheme(
        colorScheme = SophisticatedColorScheme,
        typography = Typography,
        content = content
    )
}
