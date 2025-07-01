package com.carlosjimz87.wandertrack.ui.composables.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

@Composable
fun DetectUserMapMovement(
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