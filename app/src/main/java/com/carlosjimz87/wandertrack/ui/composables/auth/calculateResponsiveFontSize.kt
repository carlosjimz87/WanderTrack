package com.carlosjimz87.wandertrack.ui.composables.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp

@Composable
fun calculateResponsiveFontSize(screenWidth: Dp): TextUnit {
    val minFontSize = 28.sp
    val maxFontSize = 82.sp
    val minScreenWidth = 320.dp
    val maxScreenWidth = 600.dp

    val progress =
        ((screenWidth - minScreenWidth) / (maxScreenWidth - minScreenWidth)).coerceIn(0f, 1f)
    return lerp(minFontSize, maxFontSize, progress)
}