package com.carlosjimz87.wandertrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorScheme = darkColorScheme(
    primary = AccentPinkDark,
    primaryContainer = AccentPink,
    onPrimary = Color.White,
    secondary = SecondaryBlueDark,
    onSecondary = Color.White,
    tertiary = TertiaryPink,
    onTertiary = Color.Black,
    background = Black,
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = SecondaryBlueDark
)

val LightColorScheme = lightColorScheme(
    primary = AccentPink,
    primaryContainer = AccentPinkDark,
    onPrimary = Color.Black,
    secondary = SecondaryBlue,
    onSecondary = Color.Black,
    tertiary = TertiaryPink,
    onTertiary = Color.Black,
    background = White,
    onBackground = Color.Black,
    surface = TertiaryNeutral,
    onSurface = Color.Black,
    surfaceVariant = SecondaryBlue
)

@Composable
fun WanderTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}