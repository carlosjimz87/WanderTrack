package com.carlosjimz87.wandertrack.utils

import com.google.android.gms.maps.model.LatLng


fun getMockCountryCodeFromLatLng(latLng: LatLng): String? {
    return if(latLng.longitude in -10.0..30.0 && latLng.latitude in 35.0..55.0) {
        "PL"
    } else null
}

fun getMockCountryBorderLatLng(code: String): List<LatLng> {
    return when (code) {
        "ES" -> listOf(
            LatLng(43.7914, -1.5561),
            LatLng(43.3947, 3.1814),
            LatLng(40.8547, 3.1044),
            LatLng(36.0000, -5.6000),
            LatLng(36.6500, -9.1000),
            LatLng(43.7914, -1.5561) // Cierra el polígono
        )
        "IT" -> listOf(
            LatLng(47.092, 6.614),
            LatLng(47.092, 13.806),
            LatLng(36.651, 13.806),
            LatLng(36.651, 6.614),
            LatLng(47.092, 6.614)
        )
        "FR" -> listOf(
            LatLng(51.0891, 2.5135),
            LatLng(51.0891, 8.2275),
            LatLng(42.3314, 8.2275),
            LatLng(42.3314, 2.5135),
            LatLng(51.0891, 2.5135)
        )
        else -> emptyList()
    }
}