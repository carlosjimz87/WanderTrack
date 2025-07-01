package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.data.repo.MapRepository
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.Logger
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    private val userId: String,
    private val mapRepo: MapRepository,
    private val firestoreRepo: FirestoreRepository,
) : ViewModel() {

    private val _userMovedMap = MutableStateFlow(false)
    val userMovedMap = _userMovedMap.asStateFlow()

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries = _countries.asStateFlow()

    private val _visitedCountryCodes = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountryCodes = _visitedCountryCodes.asStateFlow()

    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val visitedCities = _visitedCities.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, CountryGeometry>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            val countriesFromFirestore = firestoreRepo.fetchAllCountries(userId)
            _countries.value = countriesFromFirestore

            _visitedCountryCodes.value = countriesFromFirestore
                .filter { it.visited }
                .map { it.code }
                .toSet()

            _visitedCities.value = countriesFromFirestore.associate { country ->
                country.code to country.cities.filter { it.visited }.map { it.name }.toSet()
            }

            _countryBorders.value = mapRepo.getCountryGeometries()

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
            mapRepo.getCountryCodeFromLatLng(latLng)
        }

        Logger.w("Resolved country from click: $code")

        val country = code?.let { getCountryByCode(_countries.value, it) }
        _selectedCountry.value = country

        return code?.let { mapRepo.getCountryBounds()[it] }
    }

    fun toggleCountryVisited(code: String) {
        val isVisitedNow = !_visitedCountryCodes.value.contains(code)

        _visitedCountryCodes.update { current ->
            if (isVisitedNow) current + code else current - code
        }

        _selectedCountry.update { current ->
            if (current?.code == code) current.copy(visited = isVisitedNow) else current
        }

        viewModelScope.launch {
            firestoreRepo.updateCountryVisited(userId, code, isVisitedNow)
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        var isNowVisited = false
        _visitedCities.update { current ->
            val currentSet = current[countryCode] ?: emptySet()
            val updatedSet = if (currentSet.contains(cityName)) {
                isNowVisited = false
                currentSet - cityName
            } else {
                isNowVisited = true
                currentSet + cityName
            }
            current + (countryCode to updatedSet)
        }

        _selectedCountry.update { current ->
            if (current?.code == countryCode) {
                val updatedCities = current.cities.map {
                    if (it.name == cityName) it.copy(visited = isNowVisited) else it
                }
                current.copy(cities = updatedCities)
            } else current
        }

        viewModelScope.launch {
            firestoreRepo.updateCityVisited(userId, countryCode, cityName, isNowVisited)
        }
    }

    fun isSameCountrySelected(latLng: LatLng): Boolean {
        val code = mapRepo.getCountryCodeFromLatLng(latLng)
        return selectedCountry.value?.code?.equals(code, ignoreCase = true) == true
    }

    fun getVisitedCountriesBounds(): LatLngBounds? {
        if (_visitedCountryCodes.value.isEmpty()) return null

        val boundsBuilder = LatLngBounds.builder()

        _visitedCountryCodes.value.forEach { code ->
            val geometry = mapRepo.getCountryGeometries()[code] ?: return@forEach
            geometry.polygons.flatten().forEach { boundsBuilder.include(it) }
        }

        return boundsBuilder.build()
    }

    fun getVisitedCountriesCenterAndBounds(): Pair<LatLng, LatLngBounds>? {
        val codes = _visitedCountryCodes.value
        if (codes.isEmpty()) return null

        val geometries = mapRepo.getCountryGeometries()

        val allPoints = codes.flatMap { code ->
            geometries[code]?.polygons?.flatten() ?: emptyList()
        }

        if (allPoints.isEmpty()) return null

        // Calcular bounds
        val boundsBuilder = LatLngBounds.builder()
        allPoints.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()

        // Calcular centroide
        val avgLat = allPoints.map { it.latitude }.average()
        val avgLng = allPoints.map { it.longitude }.average()
        val center = LatLng(avgLat, avgLng)

        return Pair(center, bounds)
    }
}