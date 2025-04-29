package com.carlosjimz87.wandertrack.ui.composables

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            getMapAsync { googleMap ->
                onMapReady(googleMap)
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDestroy()
        }
    }
}