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
    primary = AccentPinkDark,            // Botones principales, elementos destacados
    onPrimary = Color.White,             // Color del texto sobre botones primarios

    secondary = SecondaryGreyDark,       // Elementos secundarios o botones alternativos
    onSecondary = Color.White,

    tertiary = TertiaryPink,             // Detalles, chips, elementos decorativos
    onTertiary = Color.Black,

    background = Black,                  // Fondo principal de la app
    onBackground = Color.White,          // Texto encima del fondo

    surface = Color(0xFF1E1E1E),         // Superficies tipo BottomSheet, Cards
    onSurface = Color.White              // Texto encima de las superficies
)

val LightColorScheme = lightColorScheme(
    primary = AccentPink,                // Botones principales, elementos destacados
    onPrimary = Color.Black,

    secondary = SecondaryGrey,           // Elementos secundarios o botones alternativos
    onSecondary = Color.Black,

    tertiary = TertiaryPink,             // Detalles, chips, elementos decorativos
    onTertiary = Color.Black,

    background = White,                  // Fondo principal de la app
    onBackground = Color.Black,

    surface = TertiaryNeutral,           // Superficies tipo BottomSheet, Cards
    onSurface = Color.Black
)

@Composable
fun WanderTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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