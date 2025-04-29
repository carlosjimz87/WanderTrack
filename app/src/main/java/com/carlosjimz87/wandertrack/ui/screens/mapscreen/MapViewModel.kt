package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    val context: Context
) : ViewModel() {

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

    private val _countryBounds = mutableMapOf<String, LatLngBounds>()
    val countryBounds: Map<String, LatLngBounds> get() = _countryBounds

    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val visitedCities = _visitedCities.asStateFlow()

    init {
        getVisitedCountriesList()
        loadCountriesGeoJson()
    }

    fun notifyUserMovedMap() {
        _userMovedMap.value = true
    }

    fun resetUserMovedFlag() {
        _userMovedMap.value = false
    }

    private fun getVisitedCountriesList() {
        _visitedCountries.value = _countries.value.filter { it.visited }.map { it.code }.toSet()
    }

    private fun loadCountriesGeoJson() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val parsedBorders = fetchCountriesGeoJson(context).toMutableMap()
            _countryBorders.value = parsedBorders

            parsedBorders.forEach { (code, polygons) ->
                val boundsBuilder = LatLngBounds.Builder()
                polygons.forEach { polygon ->
                    polygon.forEach { point ->
                        boundsBuilder.include(point)
                    }
                }
                _countryBounds[code] = boundsBuilder.build()
            }

            _isLoading.value = false
        }
    }

    fun toggleCountryVisited(code: String) {
        _visitedCountries.update { current ->
            if (current.contains(code)) current - code else current + code
        }
    }

    fun clearSelectedCountry() {
        _selectedCountry.value = null
    }

    fun resolveCountryFromLatLng(latLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val countryCode = getCountryCodeFromLatLngOffline(latLng)
            countryCode?.let { code ->
                getCountryByCode(code)?.let { country ->
                    _selectedCountry.value = country
                    Log.d("MapViewModel", "Resolved country: $country")
                }
            }
        }
    }

    private fun getCountryByCode(code: String): Country? {
        return countries.value.find { it.code.equals(code, ignoreCase = true) }
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


    private fun getCountryCodeFromLatLngOffline(latLng: LatLng): String? {
        for ((code, polygons) in countryBorders.value) {
            for (polygon in polygons) {
                if (PolyUtil.containsLocation(latLng, polygon, true)) {
                    return code
                }
            }
        }
        return null
    }
}