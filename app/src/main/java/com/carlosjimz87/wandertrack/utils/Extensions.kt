package com.carlosjimz87.wandertrack.utils

import com.google.android.gms.maps.model.LatLng


fun getMockCountryCodeFromLatLng(latLng: LatLng): String? {
    return if(latLng.longitude in -10.0..30.0 && latLng.latitude in 35.0..55.0) {
        "PL"
    } else null
}