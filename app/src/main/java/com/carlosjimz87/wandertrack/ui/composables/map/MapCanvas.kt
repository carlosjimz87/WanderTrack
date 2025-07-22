package com.carlosjimz87.wandertrack.ui.composables.map

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.Constants.MAX_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.common.Constants.MIN_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.models.map.PolygonData
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

    val visitedPolygons by remember(visitedCountriesCodes, countryBorders) {
        derivedStateOf {
            visitedCountriesCodes.flatMap { code ->
                countryBorders[code]?.polygons?.map { polygon ->
                    PolygonData(polygon)
                } ?: emptyList()
            }
        }
    }

    val selectedPolygons by remember(selectedCountry, countryBorders) {
        derivedStateOf {
            selectedCountry?.let { country ->
                countryBorders[country.code]?.polygons?.map { polygon ->
                    PolygonData(polygon, isSelected = true)
                }
            } ?: emptyList()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = MapProperties(
            mapStyleOptions = getMapStyle(context),
            isBuildingEnabled = false,
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            minZoomPreference = MIN_ZOOM_LEVEL,
            maxZoomPreference = MAX_ZOOM_LEVEL
        ),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = false
        ),
        onMapClick = onMapClick
    ) {
        visitedPolygons.forEach { polygonData ->
            Polygon(
                points = polygonData.points,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                strokeColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 4f
            )
        }
        selectedPolygons.forEach { polygonData ->
            Polygon(
                points = polygonData.points,
                fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                strokeColor = MaterialTheme.colorScheme.secondary,
                strokeWidth = 4f,
                zIndex = 2f
            )
        }
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
