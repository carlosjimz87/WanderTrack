package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val context = LocalContext.current
    val visitedCountries by viewModel.visitedCountries.collectAsState()
    val countryBorders by viewModel.countryBorders.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDarkTheme = isSystemInDarkTheme()
    val mapStyle = if (isDarkTheme) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
    } else {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }

    var lastClickLatLng by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(cameraPositionState.position.target) {
        lastClickLatLng?.let { original ->
            val movedDistance = SphericalUtil.computeDistanceBetween(
                original,
                cameraPositionState.position.target
            )
            if (movedDistance > 100_000) { // más de 100 km
                viewModel.notifyUserMovedMap()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(
                mapStyleOptions = mapStyle,
                isBuildingEnabled = false,
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false
            ),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                mapToolbarEnabled = false
            ),
            onMapClick = { latLng ->
                lastClickLatLng = latLng
                viewModel.resetUserMovedFlag()
                viewModel.onMapClick(context, latLng) // 🚀 solo guarda
            }
        ) {
            visitedCountries.forEach { code ->
                val polygons = countryBorders[code] ?: return@forEach
                polygons.forEach { polygon ->
                    Polygon(
                        points = polygon,
                        fillColor = AccentPink.copy(alpha = 0.5f),
                        strokeColor = AccentPink,
                        strokeWidth = 4f
                    )
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        }

        selectedCountry?.let { country ->
            LaunchedEffect(country) {
                val bounds = viewModel.countryBounds[country.code]
                if (bounds != null && !viewModel.userMovedMap.value) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                    delay(300)
                }
                bottomSheetState.show()
            }

            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { viewModel.clearSelectedCountry() }
            ) {
                CountryBottomSheetContent(
                    country = country,
                    visitedCities = viewModel.visitedCities.value[country.code] ?: emptySet(),
                    onToggleCityVisited = { cityName ->
                        viewModel.toggleCityVisited(country.code, cityName)
                    },
                    onToggleVisited = { code -> viewModel.toggleCountryVisited(code) },
                    onDismiss = { viewModel.clearSelectedCountry() },
                )
            }
        }
    }

    // 📍 Hacemos la animación y búsqueda de país tras click
    LaunchedEffect(lastClickLatLng) {
        lastClickLatLng?.let { latLng ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            viewModel.resolveCountryFromLatLng(context, latLng)
        }
    }
}