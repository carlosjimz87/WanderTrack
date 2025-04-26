package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.carlosjimz87.wandertrack.utils.getMockCountryCodeFromLatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val visitedCountries by viewModel.visitedCountries.collectAsState()
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isBuildingEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = false
        ),
        onMapClick = { latLng: LatLng ->
            val countryCode = getMockCountryCodeFromLatLng(latLng)
            countryCode?.let{ onCountryClicked(it) }
        }
    ) {
        visitedCountries.forEach {
            // draw overlay for each visited country
        }

    }
}