package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

interface MapRepository {
    fun getCountryGeometries(): Map<String, CountryGeometry>
    fun getCountryBounds(): Map<String, LatLngBounds>
    fun getCountryCodeFromLatLng(latLng: LatLng): String?
}