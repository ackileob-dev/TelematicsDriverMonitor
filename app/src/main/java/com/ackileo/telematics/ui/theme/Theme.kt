@file:Suppress("DEPRECATION")

package com.ackileo.telematics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    secondary = SecondaryGreenLight,
    error = ErrorRedLight,
    background = BackgroundDark,
    surface = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    error = ErrorRed,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// In your Composable components, ALWAYS use these color roles:
// Background -> MaterialTheme.colorScheme.background
// Text -> MaterialTheme.colorScheme.onBackground
// Card Surface -> MaterialTheme.colorScheme.surfaceVariant

@Composable
fun TelematicsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure this uses MaterialTheme roles
        content = content
    )
}
