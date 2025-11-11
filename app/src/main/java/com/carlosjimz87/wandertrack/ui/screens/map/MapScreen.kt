package com.carlosjimz87.wandertrack.ui.screens.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.ui.composables.bottomsheet.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.map.MapCanvas
import com.carlosjimz87.wandertrack.ui.composables.map.MapHeaderInfo
import com.carlosjimz87.wandertrack.ui.composables.map.ProfileIconButton
import com.carlosjimz87.wandertrack.ui.screens.map.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    userId: String,
    onProfileClick: () -> Unit,
) {
    val viewModel: MapViewModel = koinViewModel { parametersOf(userId) }
    val uiState by viewModel.uiState.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()

    val cameraPositionState = rememberCameraPositionState {
        position = uiState.lastCameraPosition
            ?: CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    var hasCenteredOnVisited by remember { mutableStateOf(false) }

    // Zoom inicial a la unión de países visitados
    LaunchedEffect(uiState.visitedUnionBounds) {
        val bounds = uiState.visitedUnionBounds ?: return@LaunchedEffect
        if (!hasCenteredOnVisited) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            hasCenteredOnVisited = true
        }
    }

    // Enfocar país seleccionado
    LaunchedEffect(uiState.selectedCountry) {
        val country = uiState.selectedCountry ?: return@LaunchedEffect
        val geom = uiState.countryBorders[country.code] ?: return@LaunchedEffect
        val allPoints = geom.polygons.flatten()
        if (allPoints.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply { allPoints.forEach { include(it) } }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            scaffoldState.bottomSheetState.expand()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        val peekHeight by animateDpAsState(
            targetValue = if (uiState.selectedCountry != null) 130.dp else 0.dp,
            label = "sheetPeek"
        )
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetContent = {
                val country = uiState.selectedCountry
                if (country != null) {
                    CountryBottomSheetContent(
                        countryName = country.name,
                        countryCode = country.code,
                        countryVisited = country.visited,
                        countryCities = country.cities,
                        onToggleCityVisited = { cityName ->
                            viewModel.toggleCityVisited(country.code, cityName)
                        },
                        onToggleVisited = { code ->
                            viewModel.toggleCountryVisited(code)
                        }
                    )
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                MapCanvas(
                    countries = uiState.countries,
                    visitedCountryCodes = uiState.visitedCountryCodes,
                    countryBorders = uiState.countryBorders,
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> viewModel.onMapClick(latLng) },
                    onUserMove = { viewModel.notifyUserMovedMap(cameraPositionState.position) },
                    isSameCountrySelected = { latLng -> viewModel.isSameCountrySelected(latLng) },
                    selectedCountry = uiState.selectedCountry
                )

                MapHeaderInfo(
                    selectedCountry = uiState.selectedCountry,
                    visitedCountriesCount = uiState.visitedCountryCodes.size,
                    visitedCitiesCount = uiState.selectedCountry?.cities?.count { it.visited } ?: 0,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )

                ProfileIconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }
}