package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.ui.composables.bottomsheet.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.map.MapCanvas
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val visitedCountriesCodes by viewModel.visitedCountryCodes.collectAsState()
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

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        },
        sheetContent = {
            selectedCountry?.let { country ->
                CountryBottomSheetContent(
                    countryName = country.name,
                    countryCode = country.code,
                    countryVisited = country.visited,
                    countryCities = country.cities,
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
            MapCanvas(
                selectedCountry = selectedCountry,
                visitedCountriesCodes = visitedCountriesCodes,
                countryBorders = countryBorders,
                onMapClick = { latLng ->
                    lastClickLatLng = latLng
                    viewModel.resetUserMovedFlag()

                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand() // 1. animate sheet first
                        delay(300) // slight delay to improve perceived perf
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 5f)) // 2. zoom
                        viewModel.resolveCountryFromLatLng(latLng) // 3. resolve
                    }
                },
                cameraPositionState = cameraPositionState
            )

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