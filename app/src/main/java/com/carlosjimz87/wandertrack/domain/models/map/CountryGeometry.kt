package com.carlosjimz87.wandertrack.domain.models.map

import com.google.android.gms.maps.model.LatLng

data class CountryGeometry(
    val polygons: List<List<LatLng>>
)