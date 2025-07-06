package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.common.animateFocusOnSelectedCountry
import com.carlosjimz87.wandertrack.common.animateToVisitedCountries
import com.carlosjimz87.wandertrack.common.calculateBottomOffset
import com.carlosjimz87.wandertrack.common.safeAnimateToBounds
import com.carlosjimz87.wandertrack.common.shouldAnimateFocusOnSelectedCountry
import com.carlosjimz87.wandertrack.common.shouldAnimateToVisitedCountries
import com.carlosjimz87.wandertrack.common.shouldResetFocus
import com.carlosjimz87.wandertrack.ui.composables.bottomsheet.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.map.BottomSheetDragHandle
import com.carlosjimz87.wandertrack.ui.composables.map.DetectUserMapMovement
import com.carlosjimz87.wandertrack.ui.composables.map.MapCanvas
import com.carlosjimz87.wandertrack.ui.composables.map.MapHeaderInfo
import com.carlosjimz87.wandertrack.ui.composables.map.ProfileIconButton
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.viewmodel.MapViewModel
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    userId: String,
    onProfileClick: () -> Unit,
) {
    val viewModel: MapViewModel = getViewModel(parameters = { parametersOf(userId) })
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
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidth = containerSize.width
    val screenHeight = containerSize.height
    var lastClickLatLng by remember { mutableStateOf<LatLng?>(null) }

    var hasCenteredMap by remember { mutableStateOf(false) }
    var hasFocusedOnBottomSheet by remember { mutableStateOf(false) }

    context.SetBottomBarColor(AccentPink)

    DetectUserMapMovement(lastClickLatLng, cameraPositionState.position.target) {
        viewModel.notifyUserMovedMap()
    }

    LaunchedEffect(
        visitedCountriesCodes,
        bottomSheetScaffoldState.bottomSheetState.currentValue,
        selectedCountry
    ) {
        when {
            shouldAnimateToVisitedCountries(visitedCountriesCodes, hasCenteredMap) -> {
                animateToVisitedCountries(cameraPositionState, viewModel)
                hasCenteredMap = true
            }
            shouldAnimateFocusOnSelectedCountry(
                bottomSheetScaffoldState.bottomSheetState.currentValue,
                hasFocusedOnBottomSheet,
                selectedCountry
            ) -> {
                animateFocusOnSelectedCountry(
                    cameraPositionState = cameraPositionState,
                    selectedCountry = selectedCountry!!,
                    countryBorders = countryBorders,
                    bottomSheetState = bottomSheetScaffoldState.bottomSheetState,
                    mapViewHeightPx = screenHeight,
                    density = density
                )
                hasFocusedOnBottomSheet = true
            }
            shouldResetFocus(bottomSheetScaffoldState.bottomSheetState.currentValue) -> {
                hasFocusedOnBottomSheet = false
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = { null },
        sheetContent = {
            val hiddenOffset = if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) 10.dp else 0.dp
            val maxContentWidth = 600.dp

            Column(
                modifier = Modifier
                    .offset(y = hiddenOffset)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .widthIn(max = maxContentWidth)
                    .align(Alignment.CenterHorizontally)
            ) {
                selectedCountry?.let { country ->
                    BottomSheetDragHandle(country.visited)

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
                    if (viewModel.isSameCountrySelected(latLng) &&
                        bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                    ) return@MapCanvas

                    lastClickLatLng = latLng
                    viewModel.resetUserMovedFlag()

                    coroutineScope.launch {
                        val bounds = viewModel.resolveCountryFromLatLng(latLng)
                        bottomSheetScaffoldState.bottomSheetState.expand()
                        bounds?.let {
                            safeAnimateToBounds(
                                cameraPositionState = cameraPositionState,
                                bounds = it,
                                mapWidth = screenWidth,
                                mapHeight = screenHeight,
                                bottomOffset = calculateBottomOffset(
                                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                                    screenHeight
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

            ProfileIconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
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