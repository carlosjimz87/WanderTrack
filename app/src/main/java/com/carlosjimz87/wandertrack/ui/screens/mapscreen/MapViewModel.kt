package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.data.mapper.MapRepository
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.Logger
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

    private val _userMovedMap = MutableStateFlow(false)
    val userMovedMap = _userMovedMap.asStateFlow()

    private val _countries = MutableStateFlow(Constants.countries)
    val countries = _countries.asStateFlow()

    private val _visitedCountryCodes = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountryCodes = _visitedCountryCodes.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, CountryGeometry>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    private val _countryBounds = MutableStateFlow<Map<String, LatLngBounds>>(emptyMap())
    val countryBounds = _countryBounds.asStateFlow()

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
                repository.getCountryBorders()
            }

            _countryBorders.value = parsedBorders

            _countryBounds.value = parsedBorders.mapValues { (_, geometry) ->
                geometry.polygons.flatten().fold(LatLngBounds.builder()) { builder, point ->
                    builder.include(point)
                }.build()
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

    suspend fun resolveCountryFromLatLng(latLng: LatLng): LatLngBounds? {
        val code = withContext(Dispatchers.Default) {
            getCountryCodeFromLatLngOffline(_countryBorders.value, latLng)
        }

        Logger.w("Resolved country from click: $code")
        val country = code?.let { getCountryByCode(_countries.value, it) }

        _selectedCountry.value = country

        return code?.let { _countryBounds.value[it] }
    }

    fun toggleCountryVisited(code: String) {
        val isVisitedNow = !_visitedCountryCodes.value.contains(code)

        _visitedCountryCodes.update { current ->
            if (isVisitedNow) current + code else current - code
        }

        _selectedCountry.update { current ->
            if (current?.code == code) current.copy(visited = isVisitedNow)
            else current
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

        _selectedCountry.update { current ->
            if (current?.code == countryCode) {
                val updatedCities = current.cities.map {
                    if (it.name == cityName) it.copy(visited = !it.visited) else it
                }
                current.copy(cities = updatedCities)
            } else current
        }
    }

    fun isSameCountrySelected(latLng: LatLng): Boolean {
        val code = getCountryCodeFromLatLngOffline(_countryBorders.value, latLng)
        return selectedCountry.value?.code?.equals(code, ignoreCase = true) == true
    }

}