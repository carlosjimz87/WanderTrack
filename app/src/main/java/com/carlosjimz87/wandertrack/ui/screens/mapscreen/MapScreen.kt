package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
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
    val mapStyle = remember(isDarkTheme) {
        if (isDarkTheme) R.raw.map_style_night else R.raw.map_style
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, mapStyle),
                isBuildingEnabled = false,
                mapType = MapType.NORMAL
            ),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                mapToolbarEnabled = false
            ),
            onMapClick = { latLng ->
                viewModel.onMapClick(context, latLng)
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
                countryBorders[country.code]?.let { polygons ->
                    val boundsBuilder = LatLngBounds.Builder()
                    polygons.forEach { polygon ->
                        polygon.forEach { point ->
                            boundsBuilder.include(point)
                        }
                    }
                    val bounds = boundsBuilder.build()

                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                    bottomSheetState.show()
                }
            }

            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { viewModel.clearSelectedCountry() }
            ) {
                CountryBottomSheetContent(
                    country = country,
                    onToggleVisited = { code -> viewModel.toggleCountryVisited(code) },
                    onDismiss = { viewModel.clearSelectedCountry() },
                    onCityClicked = {}
                )
            }
        }
    }
}