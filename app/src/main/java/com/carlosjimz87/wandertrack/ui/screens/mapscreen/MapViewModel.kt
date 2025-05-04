package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.data.repos.map.MapRepository
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLngOffline
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    private val repository: MapRepository
) : ViewModel() {

    // --- UI STATES ---
    private val _userMovedMap = MutableStateFlow(false)
    val userMovedMap = _userMovedMap.asStateFlow()

    private val _countries = MutableStateFlow(Constants.countries)
    val countries = _countries.asStateFlow()

    private val _visitedCountryCodes = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountryCodes = _visitedCountryCodes.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, List<List<LatLng>>>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val visitedCities = _visitedCities.asStateFlow()

    init {
        markInitiallyVisitedCountries()
        loadBordersAndPrecalculateBounds()
    }

    private fun markInitiallyVisitedCountries() {
        _visitedCountryCodes.value = _countries.value
            .filter { it.visited }
            .map { it.code }
            .toSet()
    }

    private fun loadBordersAndPrecalculateBounds() {
        viewModelScope.launch {
            _isLoading.value = true
            val parsedBorders = withContext(Dispatchers.IO) {
                repository.getCountryBorders()  // from MapRepository
            }

            _countryBorders.value = parsedBorders

            parsedBorders.forEach { (_, polygons) ->
                val boundsBuilder = LatLngBounds.Builder()
                polygons.flatten().forEach(boundsBuilder::include)
            }

            _isLoading.value = false
        }
    }

    fun notifyUserMovedMap() {
        _userMovedMap.value = true
    }

    fun resetUserMovedFlag() {
        _userMovedMap.value = false
    }

    fun clearSelectedCountry() {
        _selectedCountry.value = null
    }

    fun resolveCountryFromLatLng(latLng: LatLng) {
        viewModelScope.launch {
            val code = withContext(Dispatchers.Default) {
                getCountryCodeFromLatLngOffline(_countryBorders.value, latLng)
            }

            _selectedCountry.value = code?.let { getCountryByCode(_countries.value, it) }
        }
    }

    fun toggleCountryVisited(code: String) {
        _visitedCountryCodes.update { current ->
            if (current.contains(code)) current - code else current + code
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        _visitedCities.update { current ->
            val currentSet = current[countryCode] ?: emptySet()
            val updatedSet = if (currentSet.contains(cityName)) {
                currentSet - cityName
            } else {
                currentSet + cityName
            }
            current + (countryCode to updatedSet)
        }
    }
}