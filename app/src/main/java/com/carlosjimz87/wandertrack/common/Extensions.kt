package com.carlosjimz87.wandertrack.common

import android.util.Log
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
    Log.d("MapScreen", "safeAnimateToBounds bs:$bottomOffset mh:$mapHeight mw:$mapWidth")
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
            durationMs = 1000
        )
    } else {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(bounds.center, 5f),
            durationMs = 1000
        )
    }
}