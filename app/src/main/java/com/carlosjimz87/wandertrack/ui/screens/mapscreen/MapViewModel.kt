package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLng
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    val context: Context
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries = _countries.asStateFlow()

    private val _visitedCountries = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountries = _visitedCountries.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, List<List<LatLng>>>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadMockCountries()
        loadCountriesGeoJson()
    }

    private fun loadMockCountries() {
        _countries.value = Constants.visitedCountries
        _visitedCountries.value = _countries.value.filter { it.visited }.map { it.code }.toSet()
    }

    private fun loadCountriesGeoJson() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val parsedBorders = fetchCountriesGeoJson(context).toMutableMap()
            Log.w("MapViewModel", "countries: $parsedBorders")
            _countryBorders.value =  parsedBorders
            Log.d("MapViewModel", "Loaded borders: ${_countryBorders.value.keys.joinToString()}")
            _isLoading.value = false
        }
    }

    fun toggleCountryVisited(code: String) {
        _visitedCountries.update { current ->
            if (current.contains(code)) current - code else current + code
        }
    }

    fun selectCountry(country: Country) {
        _selectedCountry.value = country
    }

    fun clearSelectedCountry() {
        _selectedCountry.value = null
    }

    fun onMapClick(context: Context, latLng: LatLng) {
        viewModelScope.launch {
            runCatching {
                val countryCode = getCountryCodeFromLatLng(context, latLng)
                countryCode?.let { code ->
                    getCountryByCode(code)?.let { country ->
                        selectCountry(country)
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun getCountryByCode(code: String): Country? {
        return countries.value.find { it.code.equals(code, ignoreCase = true) }
    }

    fun selectCity(cityName: String) {
        // TODO: Marcar la ciudad como visitada si implementamos esa parte
    }
}