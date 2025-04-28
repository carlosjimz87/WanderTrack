package com.carlosjimz87.wandertrack.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.carlosjimz87.wandertrack.utils.getMockCountryBorderLatLng
import com.google.maps.android.compose.Polygon

@Composable
fun DrawCountryHighlight(
    countryCode: String,
    color: Color
) {
    val borderPoints = getMockCountryBorderLatLng(countryCode)

    if (borderPoints.isNotEmpty()) {
        Polygon(
            points = borderPoints,
            fillColor = color.copy(alpha = 0.3f),
            strokeColor = color,
            strokeWidth = 3f
        )
    }
}