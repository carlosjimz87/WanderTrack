package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.theme.AccentPinkDark
import com.carlosjimz87.wandertrack.utils.getMockCountryBorderLatLng
import com.carlosjimz87.wandertrack.utils.getMockCountryCodeFromLatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }
    val countries by viewModel.visitedCountries.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleOptions = remember(isDarkTheme) {
        if (isDarkTheme) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
        } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        }
    }
    val visitedColor = AccentPinkDark

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            mapStyleOptions = mapStyleOptions,
            isBuildingEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = false
        ),
        onMapClick = { latLng: LatLng ->
            val countryCode = getMockCountryCodeFromLatLng(latLng)
            countryCode?.let { onCountryClicked(it) }
        }
    ) {
        countries.filter { it.visited }.forEach { country ->
            val borderPoints = getMockCountryBorderLatLng(country.code)
            if (borderPoints.isNotEmpty()) {
                Polygon(
                    points = getMockCountryBorderLatLng(country.code),
                    fillColor = visitedColor.copy(alpha = 0.5f),
                    strokeColor = visitedColor,
                    strokeWidth = 4f
                )
            }
        }

    }
}