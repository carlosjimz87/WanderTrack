package com.carlosjimz87.wandertrack.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import com.carlosjimz87.wandertrack.common.Constants.ANIMATION_DURATION
import com.carlosjimz87.wandertrack.domain.models.City
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState

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

    if (isSizeSafe) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                mapWidth,
                mapHeight,
                sidePadding
            ),
            durationMs = ANIMATION_DURATION
        )
    } else {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(bounds.center, 5f),
            durationMs = ANIMATION_DURATION
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun calculateBottomOffset(
    sheetValue: SheetValue,
    mapViewHeight: Int
): Int = if (sheetValue == SheetValue.Expanded)
    mapViewHeight else mapViewHeight / 3