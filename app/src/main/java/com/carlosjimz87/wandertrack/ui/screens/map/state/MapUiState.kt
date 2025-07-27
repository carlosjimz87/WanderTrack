package com.carlosjimz87.wandertrack.ui.screens.map.state

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.google.android.gms.maps.model.CameraPosition

data class MapUiState(
    val visitedCountryCodes: Set<String> = emptySet(),
    val visitedCitiesMap: Map<String, Set<String>> = emptyMap(),
    val selectedCountry: Country? = null,
    val isLoading: Boolean = false,
    val lastCameraPosition: CameraPosition? = null,
    val countryBorders : Map<String, CountryGeometry> = emptyMap(),
)