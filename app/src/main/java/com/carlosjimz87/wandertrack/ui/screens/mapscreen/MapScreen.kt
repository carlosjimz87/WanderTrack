package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.ui.composables.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val visitedCountries by viewModel.visitedCountries.collectAsState()
    val countryBorders by viewModel.countryBorders.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    var lastClickLatLng by remember { mutableStateOf<LatLng?>(null) }

    DetectUserMapMovement(
        lastClickLatLng,
        cameraPositionState.position.target
    ) {
        viewModel.notifyUserMovedMap()
    }

    AnimateClickAndResolveCountry(
        lastClickLatLng,
        coroutineScope,
        cameraPositionState
    ) { latLng ->
        viewModel.resolveCountryFromLatLng(latLng)
    }

    HandleBottomSheetState(
        selectedCountry,
        bottomSheetScaffoldState.bottomSheetState
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
        sheetContent = {
            selectedCountry?.let { country ->
                CountryBottomSheetContent(
                    country = country,
                    visitedCities = viewModel.visitedCities.value[country.code] ?: emptySet(),
                    onToggleCityVisited = { viewModel.toggleCityVisited(country.code, it) },
                    onToggleVisited = { viewModel.toggleCountryVisited(it) },
                    onDismiss = { viewModel.clearSelectedCountry() }
                )
            } ?: Spacer(modifier = Modifier.height(1.dp))
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(
                    mapStyleOptions = getMapStyle(context),
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
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    }
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
        }
    }
}


@Composable
private fun DetectUserMapMovement(
    lastClickLatLng: LatLng?,
    cameraTarget: LatLng,
    onMoveDetected: () -> Unit
) {
    LaunchedEffect(cameraTarget) {
        lastClickLatLng?.let { original ->
            val movedDistance = SphericalUtil.computeDistanceBetween(original, cameraTarget)
            if (movedDistance > 100_000) onMoveDetected()
        }
    }
}

@Composable
private fun AnimateClickAndResolveCountry(
    lastClickLatLng: LatLng?,
    coroutineScope: CoroutineScope,
    cameraPositionState: CameraPositionState,
    onResolve: suspend (LatLng) -> Unit
) {
    LaunchedEffect(lastClickLatLng) {
        lastClickLatLng?.let { latLng ->
            coroutineScope.launch {
                delay(500)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            }
            coroutineScope.launch {
                delay(500)
                onResolve(latLng)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleBottomSheetState(
    selectedCountry: Country?,
    bottomSheetState: SheetState
) {
    LaunchedEffect(selectedCountry) {
        if (selectedCountry == null) {
            bottomSheetState.partialExpand()
        } else {
            bottomSheetState.expand()
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