package com.carlosjimz87.wandertrack.ui.screens.mapscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.managers.StoreManager
import com.carlosjimz87.wandertrack.utils.Logger
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    private val userId: String,
    private val mapRepo: MapRepository,
    private val firestoreRepo: FirestoreRepository,
    private val storeManager: StoreManager
) : ViewModel() {

    private val _userMovedMap = MutableStateFlow(false)

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries = _countries.asStateFlow()

    private val _visitedCountryCodes = MutableStateFlow<Set<String>>(emptySet())
    val visitedCountryCodes = _visitedCountryCodes.asStateFlow()

    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _countryBorders = MutableStateFlow<Map<String, CountryGeometry>>(emptyMap())
    val countryBorders = _countryBorders.asStateFlow()

    private val _lastCameraPosition = MutableStateFlow<CameraPosition?>(null)
    val lastCameraPosition: StateFlow<CameraPosition?> = _lastCameraPosition.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            val countriesFromFirestore = firestoreRepo.fetchAllCountries(userId)
            _countries.value = countriesFromFirestore

            _visitedCountryCodes.value =
                countriesFromFirestore.filter { it.visited }.map { it.code }.toSet()
            _visitedCities.value = countriesFromFirestore.associate {
                it.code to it.cities.filter { it.visited }.map { it.name }.toSet()
            }
            _countryBorders.value = mapRepo.getCountryGeometries()

            _isLoading.value = false
        }
    }

    fun notifyUserMovedMap(position: CameraPosition) {
        _userMovedMap.value = true
        _lastCameraPosition.value = position
    }

    fun resetUserMovedFlag() {
        _userMovedMap.value = false
    }

    fun clearSelectedCountry() {
        _selectedCountry.value = null
    }

    suspend fun resolveCountryFromLatLng(latLng: LatLng): LatLngBounds? {
        val code = withContext(Dispatchers.Default) { mapRepo.getCountryCodeFromLatLng(latLng) }
        Logger.w("Resolved country from click: $code")

        _selectedCountry.value = code?.let { getCountryByCode(_countries.value, it) }
        return code?.let { mapRepo.getCountryBounds()[it] }
    }

    fun toggleCountryVisited(code: String) {
        val isVisitedNow = !_visitedCountryCodes.value.contains(code)

        _visitedCountryCodes.update { if (isVisitedNow) it + code else it - code }
        updateCountryVisitedInList(code, isVisitedNow)
        updateSelectedCountryVisited(code, isVisitedNow)

        if (!isVisitedNow) {
            // 🧠 Clear visited cities for that country from visitedCities
            _visitedCities.update { current ->
                current - code
            }

            // 🧠 Unmark cities in countries list
            _countries.update { list ->
                list.map { country ->
                    if (country.code == code) {
                        country.copy(
                            cities = country.cities.map { it.copy(visited = false) }
                        )
                    } else country
                }
            }

            // 🧠 Unmark cities in selectedCountry
            _selectedCountry.update { current ->
                if (current?.code == code) {
                    current.copy(
                        cities = current.cities.map { it.copy(visited = false) }
                    )
                } else current
            }
        }

        viewModelScope.launch {
            firestoreRepo.updateCountryVisited(userId, code, isVisitedNow)
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        val isNowVisited = _visitedCities.value[countryCode]?.contains(cityName) != true

        _visitedCities.update { current ->
            val updatedSet = (current[countryCode] ?: emptySet()).let {
                if (isNowVisited) it + cityName else it - cityName
            }
            current + (countryCode to updatedSet)
        }

        updateCityVisitedInList(countryCode, cityName, isNowVisited)
        updateSelectedCityVisited(countryCode, cityName, isNowVisited)

        viewModelScope.launch {
            firestoreRepo.updateCityVisited(userId, countryCode, cityName, isNowVisited)

            val remainingCities = _visitedCities.value[countryCode]?.size ?: 0
            val countryVisited = _visitedCountryCodes.value.contains(countryCode)

            if (isNowVisited && !countryVisited) {
                toggleCountryVisited(countryCode)
            } else if (!isNowVisited && remainingCities == 0 && countryVisited) {
                toggleCountryVisited(countryCode)
            }
        }
    }

    fun isSameCountrySelected(latLng: LatLng): Boolean {
        val code = mapRepo.getCountryCodeFromLatLng(latLng)
        return selectedCountry.value?.code?.equals(code, ignoreCase = true) == true
    }

    fun getVisitedCountriesCenterAndBounds(): Pair<LatLng, LatLngBounds>? {
        val codes = _visitedCountryCodes.value
        if (codes.isEmpty()) return null

        val allPoints =
            codes.flatMap { mapRepo.getCountryGeometries()[it]?.polygons?.flatten().orEmpty() }
        if (allPoints.isEmpty()) return null

        val boundsBuilder = LatLngBounds.builder().apply { allPoints.forEach { include(it) } }
        val bounds = boundsBuilder.build()

        val center = LatLng(
            allPoints.map { it.latitude }.average(),
            allPoints.map { it.longitude }.average()
        )
        return center to bounds
    }

    private fun updateCountryVisitedInList(code: String, visited: Boolean) {
        _countries.update { list ->
            list.map { if (it.code == code) it.copy(visited = visited) else it }
        }
    }

    private fun updateSelectedCountryVisited(code: String, visited: Boolean) {
        _selectedCountry.update { current ->
            if (current?.code == code) current.copy(visited = visited) else current
        }
    }

    private fun updateCityVisitedInList(countryCode: String, cityName: String, visited: Boolean) {
        _countries.update { list ->
            list.map { country ->
                if (country.code == countryCode) {
                    country.copy(cities = country.cities.map {
                        if (it.name == cityName) it.copy(visited = visited) else it
                    })
                } else country
            }
        }
    }

    private fun updateSelectedCityVisited(countryCode: String, cityName: String, visited: Boolean) {
        _selectedCountry.update { current ->
            if (current?.code == countryCode) {
                current.copy(cities = current.cities.map {
                    if (it.name == cityName) it.copy(visited = visited) else it
                })
            } else current
        }
    }
}