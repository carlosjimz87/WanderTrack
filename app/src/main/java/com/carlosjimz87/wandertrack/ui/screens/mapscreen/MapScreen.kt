package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.common.Constants.ANIMATION_DURATION
import com.carlosjimz87.wandertrack.common.calculateBottomOffset
import com.carlosjimz87.wandertrack.common.calculateZoomLevel
import com.carlosjimz87.wandertrack.common.safeAnimateToBounds
import com.carlosjimz87.wandertrack.ui.composables.bottomsheet.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.map.MapCanvas
import com.carlosjimz87.wandertrack.ui.composables.map.MapHeaderInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.rememberCameraPositionState
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
    val context = LocalContext.current
    val containerSize = LocalWindowInfo.current.containerSize
    val configuration = LocalConfiguration.current
    val mapViewHeight = remember(configuration) { containerSize.height }
    val mapViewWidth = remember(configuration) { containerSize.width }
    var lastClickLatLng by remember { mutableStateOf<LatLng?>(null) }

    var focusedOnBottomSheet by remember { mutableStateOf(false) }
    var hasCenteredMap by remember { mutableStateOf(false) }

    DetectUserMapMovement(
        lastClickLatLng,
        cameraPositionState.position.target
    ) {
        viewModel.notifyUserMovedMap()
    }

    // Centrar mapa en países visitados solo una vez al cargar
    LaunchedEffect(visitedCountriesCodes) {
        if (visitedCountriesCodes.isNotEmpty() && !hasCenteredMap) {
            viewModel.getVisitedCountriesCenterAndBounds()?.let { (center, bounds) ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(center, calculateZoomLevel(bounds)),
                    durationMs = Constants.ANIMATION_DURATION
                )
            }
            hasCenteredMap = true
        }
    }

    // Ajustar foco del país seleccionado cuando el BottomSheet se abre
    LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
        if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded && !focusedOnBottomSheet) {
            selectedCountry?.let { country ->
                val boundsBuilder = LatLngBounds.builder()
                val geometry = countryBorders[country.code]
                geometry?.polygons?.flatten()?.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()

                val latOffset = (bounds.northeast.latitude - bounds.southwest.latitude) * 0.25 // ajustar según altura BottomSheet

                val adjustedCenter = LatLng(
                    bounds.center.latitude + latOffset,
                    bounds.center.longitude
                )

                val zoomLevel = calculateZoomLevel(bounds)

                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(adjustedCenter, zoomLevel),
                    durationMs = ANIMATION_DURATION
                )

                focusedOnBottomSheet = true
            }
        } else if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
            // Reset flag cuando BottomSheet se oculta para permitir nuevo foco después
            focusedOnBottomSheet = false
        }
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .systemBarsPadding()
        ) {
            MapCanvas(
                selectedCountry = selectedCountry,
                visitedCountriesCodes = visitedCountriesCodes,
                countryBorders = countryBorders,
                onMapClick = { latLng ->
                    if (viewModel.isSameCountrySelected(latLng)) return@MapCanvas

                    lastClickLatLng = latLng
                    viewModel.resetUserMovedFlag()

                    coroutineScope.launch {
                        val bounds = viewModel.resolveCountryFromLatLng(latLng)
                        bottomSheetScaffoldState.bottomSheetState.expand()

                        bounds?.let {
                            safeAnimateToBounds(
                                cameraPositionState = cameraPositionState,
                                bounds = it,
                                mapWidth = mapViewWidth,
                                mapHeight = mapViewHeight,
                                bottomOffset = calculateBottomOffset(
                                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                                    mapViewHeight
                                )
                            )
                        }
                    }
                },
                cameraPositionState = cameraPositionState
            )

            MapHeaderInfo(
                selectedCountry = selectedCountry,
                visitedCountriesCount = visitedCountriesCodes.size,
                modifier = Modifier.align(Alignment.TopStart)
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
