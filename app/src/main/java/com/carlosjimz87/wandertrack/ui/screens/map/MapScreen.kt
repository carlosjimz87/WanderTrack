package com.carlosjimz87.wandertrack.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.common.animateFocusOnSelectedCountry
import com.carlosjimz87.wandertrack.common.animateToVisitedCountries
import com.carlosjimz87.wandertrack.common.calculateBottomOffset
import com.carlosjimz87.wandertrack.common.safeAnimateToBounds
import com.carlosjimz87.wandertrack.common.shouldAnimateFocusOnSelectedCountry
import com.carlosjimz87.wandertrack.common.shouldAnimateToVisitedCountries
import com.carlosjimz87.wandertrack.common.shouldResetFocus
import com.carlosjimz87.wandertrack.domain.models.Screens
import com.carlosjimz87.wandertrack.managers.StylesManager
import com.carlosjimz87.wandertrack.ui.composables.bottomsheet.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.map.BottomSheetDragHandle
import com.carlosjimz87.wandertrack.ui.composables.map.MapCanvas
import com.carlosjimz87.wandertrack.ui.composables.map.MapHeaderInfo
import com.carlosjimz87.wandertrack.ui.composables.map.ProfileIconButton
import com.carlosjimz87.wandertrack.ui.screens.map.viewmodel.MapViewModel
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    userId: String,
    from: Screens = Screens.Profile,
    onProfileClick: () -> Unit,
) {
    // Injections
    val stylesManager: StylesManager = getKoin().get()
    val viewModel: MapViewModel = koinViewModel(
        key = "map-$userId",
        parameters = { parametersOf(userId) }
    )
    LaunchedEffect(viewModel, from) {
        viewModel.setFrom(from)
    }

    // Initializations
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()
    val visitedCountriesCodes by viewModel.visitedCountryCodes.collectAsState()
    val cameFrom by viewModel.cameFrom.collectAsState()
    val containerSize = LocalWindowInfo.current.containerSize
    context.SetBottomBarColor(AccentPink)

    // Collections
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val countryBorders by viewModel.countryBorders.collectAsState()
    val lastCameraPosition by viewModel.lastCameraPosition.collectAsState()

    // Remembered scopes
    val visitedCitiesCount = remember(selectedCountry) {
        selectedCountry?.cities?.count { it.visited } ?: 0
    }
    val cameraPositionState = rememberCameraPositionState {
        position = lastCameraPosition ?: CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    val composition by rememberLottieComposition(stylesManager.getAnimationStyles(isDarkTheme))

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    var hasCenteredMap by remember { mutableStateOf(false) }
    var hasFocusedOnBottomSheet by remember { mutableStateOf(false) }
    var isAnimatingCamera by remember { mutableStateOf(false) }

    // Launched effects
    LaunchedEffect(cameraPositionState) {
        var lastCamera: CameraPosition? = null

        snapshotFlow { cameraPositionState.position }
            .distinctUntilChanged()
            .collect { newPosition ->
                val camera = CameraPosition.fromLatLngZoom(newPosition.target, newPosition.zoom)

                // Only record if not animating
                if (!isAnimatingCamera &&
                    (lastCamera == null || lastCamera!!.target != camera.target || lastCamera!!.zoom != camera.zoom)
                ) {
                    viewModel.notifyUserMovedMap(camera)
                    lastCamera = camera
                }
            }
    }

    // Animate camera to initial states
    LaunchedEffect(
        visitedCountriesCodes,
        bottomSheetScaffoldState.bottomSheetState.currentValue,
        selectedCountry
    ) {
        if (cameFrom == Screens.Profile) return@LaunchedEffect

        when {
            shouldAnimateToVisitedCountries(visitedCountriesCodes, hasCenteredMap) -> {
                isAnimatingCamera = true
                animateToVisitedCountries(cameraPositionState, viewModel)
                isAnimatingCamera = false
                hasCenteredMap = true
            }

            shouldAnimateFocusOnSelectedCountry(
                bottomSheetScaffoldState.bottomSheetState.currentValue,
                hasFocusedOnBottomSheet,
                selectedCountry
            ) -> {
                isAnimatingCamera = true
                animateFocusOnSelectedCountry(
                    cameraPositionState = cameraPositionState,
                    selectedCountry = selectedCountry!!,
                    countryBorders = countryBorders,
                    bottomSheetState = bottomSheetScaffoldState.bottomSheetState,
                    mapViewHeightPx = containerSize.height,
                    density = density
                )
                isAnimatingCamera = false
                hasFocusedOnBottomSheet = true
            }

            shouldResetFocus(bottomSheetScaffoldState.bottomSheetState.currentValue) -> {
                hasFocusedOnBottomSheet = false
            }
        }
    }

    // Ui components
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
        sheetDragHandle = { null },
        sheetContent = {
            val hiddenOffset =
                if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) 10.dp else 0.dp
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
                        onToggleCityVisited = {
                            viewModel.toggleCityVisited(country.code, it)
                                              },
                        onToggleVisited = {
                            viewModel.toggleCountryVisited(it)
                        }
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

                    coroutineScope.launch {
                        val bounds = viewModel.resolveCountryFromLatLng(latLng)
                        bottomSheetScaffoldState.bottomSheetState.expand()
                        bounds?.let {
                            isAnimatingCamera = true
                            safeAnimateToBounds(
                                cameraPositionState = cameraPositionState,
                                bounds = it,
                                mapWidth = containerSize.width,
                                mapHeight = containerSize.height,
                                bottomOffset = calculateBottomOffset(
                                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                                    containerSize.height
                                )
                            )
                            isAnimatingCamera = false
                        }
                    }
                },
                cameraPositionState = cameraPositionState
            )

            MapHeaderInfo(
                selectedCountry = selectedCountry,
                visitedCitiesCount = visitedCitiesCount,
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(150.dp),
                    )
                }
            }
        }
    }
}