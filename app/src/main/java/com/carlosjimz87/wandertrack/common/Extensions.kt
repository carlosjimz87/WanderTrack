package com.carlosjimz87.wandertrack.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import com.carlosjimz87.wandertrack.common.Constants.ANIMATION_DURATION
import com.carlosjimz87.wandertrack.common.Constants.MIN_ZOOM_LEVEL
import com.carlosjimz87.wandertrack.domain.models.City
import com.google.android.gms.maps.CameraUpdateFactory
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