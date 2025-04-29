package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLngOffline
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    val context: Context
) : ViewModel() {

    // --- UI STATES ---
    private val _userMovedMap = MutableStateFlow(false)
    val userMovedMap = _userMovedMap.asStateFlow()

    private val _countries = MutableStateFlow(Constants.countries)
    val countries = _countries.asStateFlow()

    private val _visitedCountries = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountries = _visitedCountries.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, List<List<LatLng>>>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val visitedCities = _visitedCities.asStateFlow()

    // --- Non-state data (precálculo) ---
    private val _countryBounds = mutableMapOf<String, LatLngBounds>()
    val countryBounds: Map<String, LatLngBounds> get() = _countryBounds

    // --- INIT ---
    init {
        getVisitedCountriesList()
        loadCountriesGeoJson()
    }

    // --- USER INTERACTION ---
    fun notifyUserMovedMap() {
        _userMovedMap.value = true
    }

    fun resetUserMovedFlag() {
        _userMovedMap.value = false
    }

    fun clearSelectedCountry() {
        _selectedCountry.value = null
    }

    // --- COUNTRY SELECTION ---
    fun resolveCountryFromLatLng(latLng: LatLng) {
        viewModelScope.launch {
            val code = withContext(Dispatchers.IO) {
                getCountryCodeFromLatLngOffline(countryBorders.value, latLng)
            }

            _selectedCountry.value = code?.let { getCountryByCode(countries.value, it) }
        }
    }

    // --- TOGGLES ---
    fun toggleCountryVisited(code: String) {
        _visitedCountries.update { current ->
            if (current.contains(code)) current - code else current + code
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        _visitedCities.update { current ->
            val currentVisited = current[countryCode] ?: emptySet()
            val updatedVisited = if (currentVisited.contains(cityName)) {
                currentVisited - cityName
            } else {
                currentVisited + cityName
            }
            current + (countryCode to updatedVisited)
        }
    }

    // --- DATA LOADING ---
    private fun getVisitedCountriesList() {
        _visitedCountries.value = _countries.value
            .filter { it.visited }
            .map { it.code }
            .toSet()
    }

    private fun loadCountriesGeoJson() {
        viewModelScope.launch {
            _isLoading.value = true

            val parsedBorders = withContext(Dispatchers.IO) {
                fetchCountriesGeoJson(context).toMutableMap()
            }

            _countryBorders.value = parsedBorders

            // Precalculate bounds
            parsedBorders.forEach { (code, polygons) ->
                val boundsBuilder = LatLngBounds.Builder()
                polygons.flatten().forEach(boundsBuilder::include)
                _countryBounds[code] = boundsBuilder.build()
            }

            _isLoading.value = false
        }
    }
}