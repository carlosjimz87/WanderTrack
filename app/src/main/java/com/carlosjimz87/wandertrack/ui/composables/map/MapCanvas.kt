package com.carlosjimz87.wandertrack.ui.composables.map

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon

@Composable
fun MapCanvas(
    visitedCountriesCodes: Set<String>,
    selectedCountry: Country?,
    countryBorders: Map<String, CountryGeometry>,
    onMapClick: (LatLng) -> Unit,
    cameraPositionState: CameraPositionState,
) {
    val context = LocalContext.current

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = MapProperties(
            mapStyleOptions = getMapStyle(context),
            isBuildingEnabled = false,
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            minZoomPreference = 2f,
            maxZoomPreference = 6f
        ),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = false
        ),
        onMapClick = onMapClick
    ) {
        PaintVisitedCountries(visitedCountriesCodes, countryBorders)
        PaintSelectedCountry(selectedCountry, countryBorders)
    }
}


@Composable
private fun getMapStyle(context: Context): MapStyleOptions {
    val isDarkTheme = isSystemInDarkTheme()
    return if (isDarkTheme) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
    } else {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
}


@Composable
private fun PaintSelectedCountry(
    selectedCountry: Country?,
    countryBorders: Map<String, CountryGeometry>
) {
    selectedCountry?.let { country ->
        val polygons = countryBorders[country.code]?.polygons ?: emptyList()
        polygons.forEach { polygon ->
            Polygon(
                points = polygon,
                fillColor = Color.Gray.copy(alpha = 0.4f),
                strokeColor = Color.DarkGray,
                strokeWidth = 4f,
                zIndex = 2f // aseguramos que quede encima
            )
        }
    }
}


@Composable
private fun PaintVisitedCountries(
    visitedCountries: Set<String>,
    countryBorders: Map<String, CountryGeometry>
) {
    visitedCountries.forEach { code ->
        val polygons = countryBorders[code] ?: return@forEach
        polygons.polygons.forEach { polygon ->
            Polygon(
                points = polygon,
                fillColor = AccentPink.copy(alpha = 0.5f),
                strokeColor = AccentPink,
                strokeWidth = 4f
            )
        }
    }
}

