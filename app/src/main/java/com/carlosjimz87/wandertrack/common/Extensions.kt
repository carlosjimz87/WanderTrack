package com.carlosjimz87.wandertrack.common

import android.app.Activity
import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.carlosjimz87.wandertrack.common.Constants.ANIMATION_DURATION
import com.carlosjimz87.wandertrack.common.Constants.MIN_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.viewmodel.MapViewModel
import com.carlosjimz87.wandertrack.ui.theme.White
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import kotlin.math.abs
import kotlin.math.max

fun List<City>.visited(): List<City> = this.filter { it.visited }.toList()

suspend fun safeAnimateToBounds(
    cameraPositionState: CameraPositionState,
    bounds: LatLngBounds,
    mapWidth: Int,
    mapHeight: Int,
    bottomOffset: Int
) {
    val topPadding = 64
    val sidePadding = 64

    val effectiveHeight = mapHeight - bottomOffset - topPadding
    val effectiveWidth = mapWidth - (sidePadding * 2)

    val isSizeSafe = effectiveHeight > 100 && effectiveWidth > 100

    val extraPadding = 100  // Padding extra para dispersión

    if (isSizeSafe) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                mapWidth,
                mapHeight,
                sidePadding + extraPadding
            ),
            durationMs = ANIMATION_DURATION
        )
    } else {
        val zoomLevel = calculateZoomLevel(bounds)
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(bounds.center, zoomLevel),
            durationMs = ANIMATION_DURATION
        )
    }
}

fun calculateZoomLevel(bounds: LatLngBounds): Float {
    val latDiff = abs(bounds.northeast.latitude - bounds.southwest.latitude)
    val lngDiff = abs(bounds.northeast.longitude - bounds.southwest.longitude)
    val sizeFactor = max(latDiff, lngDiff)

    val zoom = when {
        sizeFactor > 40 -> 2.5f
        sizeFactor > 20 -> 3.5f
        sizeFactor > 10 -> 4.5f
        sizeFactor > 5  -> 5.5f
        sizeFactor > 2  -> 6.5f
        else -> 7.5f
    }
    return max(zoom, MIN_ZOOM_LEVEL)
}

@OptIn(ExperimentalMaterial3Api::class)
fun calculateBottomOffset(
    sheetValue: SheetValue,
    mapViewHeight: Int
): Int = if (sheetValue == SheetValue.Expanded)
    mapViewHeight else mapViewHeight / 3

fun shouldAnimateToVisitedCountries(
    visitedCountriesCodes: Set<String>,
    hasCenteredMap: Boolean
): Boolean = visitedCountriesCodes.isNotEmpty() && !hasCenteredMap

@OptIn(ExperimentalMaterial3Api::class)
fun shouldAnimateFocusOnSelectedCountry(
    sheetStateCurrentValue: SheetValue,
    hasFocusedOnBottomSheet: Boolean,
    selectedCountry: Country?
): Boolean =
    sheetStateCurrentValue == SheetValue.Expanded && !hasFocusedOnBottomSheet && selectedCountry != null

@OptIn(ExperimentalMaterial3Api::class)
fun shouldResetFocus(sheetStateCurrentValue: SheetValue): Boolean =
    sheetStateCurrentValue == SheetValue.Hidden

suspend fun animateToVisitedCountries(
    cameraPositionState: CameraPositionState,
    viewModel: MapViewModel
) {
    viewModel.getVisitedCountriesCenterAndBounds()?.let { (center, bounds) ->
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(center, calculateZoomLevel(bounds)),
            durationMs = ANIMATION_DURATION
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
suspend fun animateFocusOnSelectedCountry(
    cameraPositionState: CameraPositionState,
    selectedCountry: Country,
    countryBorders: Map<String, CountryGeometry>,
    bottomSheetState: SheetState,
    mapViewHeightPx: Int,
    density: Density
) {
    val geometry = countryBorders[selectedCountry.code] ?: return

    val boundsBuilder = LatLngBounds.builder()
    geometry.polygons.flatten().forEach { boundsBuilder.include(it) }
    val bounds = boundsBuilder.build()

    // Altura ocupada por el BottomSheet en píxeles según su estado
    val bottomSheetHeightPx = when (bottomSheetState.currentValue) {
        SheetValue.Expanded -> mapViewHeightPx / 2       // Ajusta según tu diseño
        SheetValue.PartiallyExpanded -> mapViewHeightPx / 3
        else -> 0
    }

    // Convertir px a dp para usar valores consistentes
    val bottomSheetHeightDp = with(density) { bottomSheetHeightPx.toDp() }
    val mapViewHeightDp = with(density) { mapViewHeightPx.toDp() }

    val latRange = bounds.northeast.latitude - bounds.southwest.latitude

    // Calcula offset para mover el centro del país hacia arriba (para no quedar oculto)
    val latOffset = latRange * (bottomSheetHeightDp.value / mapViewHeightDp.value) * 1.3f // factor ajustable

    val adjustedCenter = LatLng(
        bounds.center.latitude + latOffset,
        bounds.center.longitude
    )

    val zoomLevel = calculateZoomLevel(bounds)

    cameraPositionState.animate(
        CameraUpdateFactory.newLatLngZoom(adjustedCenter, zoomLevel),
        durationMs = ANIMATION_DURATION
    )
}

@Composable
fun Context.SetBottomBarColor(color: Color = White){
    val window = (this as Activity).window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    SideEffect {
        window.navigationBarColor = color.toArgb() // Your red splash color
    }
}