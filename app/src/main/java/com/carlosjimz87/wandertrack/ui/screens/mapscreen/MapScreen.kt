package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.ui.composables.CountryBottomSheetContent
import com.carlosjimz87.wandertrack.ui.composables.DrawCountryHighlight
import com.carlosjimz87.wandertrack.ui.theme.AccentPinkDark
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLng
import com.carlosjimz87.wandertrack.utils.getMockCountryBorderLatLng
import com.carlosjimz87.wandertrack.utils.getMockCountryCenterLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onCountryClicked: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }
    val countries by viewModel.countries.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleOptions = remember(isDarkTheme) {
        if (isDarkTheme) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
        } else {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        }
    }
    val visitedColor = AccentPinkDark
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCountry by remember { mutableStateOf<Country?>(null) }
    val scaffoldState = rememberBottomSheetScaffoldState()


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            selectedCountry?.let { country ->
                CountryBottomSheetContent(
                    country = country,
                    onToggleVisited = { countryCode ->
                        viewModel.toggleCountryVisited(countryCode)
                    },
                    onCityClicked = { cityName ->
                        // TODO: Navegar a CityScreen
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            selectedCountry = null
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                )
            } ?: Spacer(modifier = Modifier.height(1.dp))
        }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyleOptions,
                isBuildingEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                mapToolbarEnabled = false
            ),
            onMapClick = { latLng ->
                coroutineScope.launch {
                    val countryCode = getCountryCodeFromLatLng(context, latLng)
                    if (countryCode != null) {
                        val center = getMockCountryCenterLatLng(countryCode)
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(center, 5.5f)
                        )

                        selectedCountry = viewModel.getCountryByCode(countryCode)
                        bottomSheetState.show()
                    }
                }
            }
        ) {
            countries.filter { it.visited }.forEach { country ->
                val borderPoints = getMockCountryBorderLatLng(country.code)
                if (borderPoints.isNotEmpty()) {
                    Polygon(
                        points = borderPoints,
                        fillColor = visitedColor.copy(alpha = 0.5f),
                        strokeColor = visitedColor,
                        strokeWidth = 4f
                    )
                }

                // 2. Dibujamos el país seleccionado (highlight activo)
                selectedCountry?.let { country ->
                    DrawCountryHighlight(
                        countryCode = country.code,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}