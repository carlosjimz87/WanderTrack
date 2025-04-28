package com.carlosjimz87.wandertrack.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds


fun getCountryCodeFromLatLng(context: Context, latLng: LatLng): String? {
    return try {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.countryCode
    } catch (e: Exception) {
        null
    }
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

fun calculateLatLngBounds(points: List<LatLng>): LatLngBounds {
    val builder = LatLngBounds.builder()
    points.forEach { builder.include(it) }
    return builder.build()
}

fun getMockCountryCenterLatLng(code: String): LatLng {
    return when (code.uppercase()) {
        "ES" -> LatLng(40.0, -4.0)    // España
        "IT" -> LatLng(42.5, 12.5)    // Italia
        "FR" -> LatLng(46.0, 2.0)     // Francia
        "DE" -> LatLng(51.0, 10.0)    // Alemania
        "PL" -> LatLng(52.0, 19.0)    // Polonia
        else -> LatLng(20.0, 0.0)     // Centro del mundo (fallback)
    }
}