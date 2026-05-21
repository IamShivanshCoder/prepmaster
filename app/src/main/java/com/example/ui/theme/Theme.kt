package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PrepPapersColorScheme = darkColorScheme(
    primary = PrimaryAccentAmber,
    secondary = SecondaryViolet,
    tertiary = SecondaryViolet,
    background = BackgroundDeepNavy,
    surface = SurfaceNavy,
    onPrimary = BackgroundDeepNavy,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = CustomGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Hardcode to dark theme only
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // We enforce our premium dark palette as requested
    MaterialTheme(
        colorScheme = PrepPapersColorScheme,
        typography = Typography,
        content = content
    )
}
