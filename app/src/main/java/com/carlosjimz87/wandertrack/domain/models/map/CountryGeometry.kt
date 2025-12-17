package com.carlosjimz87.wandertrack.domain.models.map

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class CountryGeometry(
    val polygons: List<List<LatLng>>,
    val bounds: LatLngBounds? = null
)