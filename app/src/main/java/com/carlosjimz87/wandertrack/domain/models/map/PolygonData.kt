package com.carlosjimz87.wandertrack.domain.models.map

import com.google.android.gms.maps.model.LatLng

data class PolygonData(
    val points: List<LatLng>,
    val isSelected: Boolean = false
)