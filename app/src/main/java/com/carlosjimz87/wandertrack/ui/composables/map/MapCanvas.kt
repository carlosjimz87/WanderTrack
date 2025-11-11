package com.carlosjimz87.wandertrack.ui.composables.map

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import com.carlosjimz87.wandertrack.common.Constants.MAX_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.common.Constants.MIN_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.models.map.PolygonData
import com.carlosjimz87.wandertrack.managers.StylesManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import org.koin.compose.getKoin

@Composable
fun MapCanvas(
    countries: List<Country>,
    visitedCountryCodes: Set<String>,
    countryBorders: Map<String, CountryGeometry>,
    cameraPositionState: CameraPositionState,
    onMapClick: (LatLng) -> Unit,
    onUserMove: () -> Unit,
    isSameCountrySelected: (LatLng) -> Boolean,
    selectedCountry: Country? = null,
) {
    // DI
    val stylesManager: StylesManager = getKoin().get()
    val isDarkTheme = isSystemInDarkTheme()

    val mapStyle = remember(isDarkTheme) { stylesManager.getMapStyle(isDarkTheme) }

    // Retrasar un frame el renderizado del mapa para evitar el flash del estilo por defecto
    var shouldShowMap by remember { mutableStateOf(false) }
    LaunchedEffect(mapStyle) {
        withFrameNanos { shouldShowMap = true }
    }

    if (!shouldShowMap) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        return
    }

    // Polígonos visitados (memoizados)
    val visitedPolygons by remember(visitedCountryCodes, countryBorders) {
        derivedStateOf {
            visitedCountryCodes.flatMap { code ->
                countryBorders[code]?.polygons.orEmpty()
            }
        }
    }

    // Polígonos del país seleccionado (memoizados)
    val selectedPolygons by remember(selectedCountry, countryBorders) {
        derivedStateOf {
            selectedCountry?.let { country ->
                countryBorders[country.code]?.polygons.orEmpty()
            }.orEmpty()
        }
    }

    // Detectar "camera idle" y notificar hacia arriba (VM) cuando pare de moverse
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .collect { moving ->
                if (!moving) onUserMove()
            }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        properties = MapProperties(
            mapStyleOptions = mapStyle,
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
        onMapClick = { latLng ->
            // Pequeña optimización: evita recalcular si ya está seleccionado el mismo país
            if (isSameCountrySelected(latLng)) return@GoogleMap
            onMapClick(latLng)
        }
    ) {
        // Países visitados
        visitedPolygons.forEach { polygon ->
            Polygon(
                points = polygon,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                strokeColor = MaterialTheme.colorScheme.primary,
                strokeWidth = 4f
            )
        }

        // País seleccionado (por encima)
        selectedPolygons.forEach { polygon ->
            Polygon(
                points = polygon,
                fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                strokeColor = MaterialTheme.colorScheme.secondary,
                strokeWidth = 4f,
                zIndex = 2f
            )
        }
    }
}