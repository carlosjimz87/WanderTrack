package com.carlosjimz87.wandertrack.ui.screens.map.state

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class MapUiState(
    val isLoading: Boolean = true,
    val countries: List<Country> = emptyList(),
    val visitedCountryCodes: Set<String> = emptySet(),
    val visitedCities: Map<String, Set<String>> = emptyMap(),
    val selectedCountry: Country? = null,
    val countryBorders: Map<String, CountryGeometry> = emptyMap(),
    val lastCameraPosition: CameraPosition? = null,

    // NEW: precomputed camera aids
    val visitedUnionBounds: LatLngBounds? = null,
    val visitedUnionCenter: LatLng? = null,

    // Optional UX flags
    val isResolvingTap: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedCountryBorders: CountryGeometry? = selectedCountry?.let { countryBorders[it.code] }
}