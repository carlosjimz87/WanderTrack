package com.carlosjimz87.wandertrack.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng


fun getCountryCodeFromLatLng(context: Context, latLng: LatLng): String? {
    return try {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.countryCode
    } catch (e: Exception) {
        null
    }
}
