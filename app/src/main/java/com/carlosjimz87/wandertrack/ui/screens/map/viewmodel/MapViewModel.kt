package com.carlosjimz87.wandertrack.ui.screens.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.models.Screens
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.domain.usecase.GetCountriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.GetCountryGeometriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCityVisitedUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCountryVisitedUseCase
import com.carlosjimz87.wandertrack.ui.screens.map.state.MapUiState
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(
    private val userId: String,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val getCountryGeometriesUseCase: GetCountryGeometriesUseCase,
    private val updateCountryVisitedUseCase: UpdateCountryVisitedUseCase,
    private val updateCityVisitedUseCase: UpdateCityVisitedUseCase,
    private val mapRepo: MapRepository, // Still needed for some map-specific operations
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    private val _visitedCountryCodes = MutableStateFlow<Set<String>>(emptySet())
    private val _visitedCities = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val _selectedCountry = MutableStateFlow<Country?>(null)
    private val _countryBorders = MutableStateFlow<Map<String, CountryGeometry>>(emptyMap())
    private val _lastCameraPosition = MutableStateFlow<CameraPosition?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _cameFrom = MutableStateFlow<Screens>(Screens.Profile)

    // UI state composition
    val countries = _countries.asStateFlow()
    val visitedCountryCodes = _visitedCountryCodes.asStateFlow()
    val selectedCountry = _selectedCountry.asStateFlow()
    val countryBorders = _countryBorders.asStateFlow()
    val lastCameraPosition = _lastCameraPosition.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val cameFrom = _cameFrom.asStateFlow()

    private val uiState = combine(
        _selectedCountry, _isLoading, _countryBorders, _lastCameraPosition
    ) { selectedCountry, isLoading, countryBorders, lastCameraPosition ->
        MapUiState(
            selectedCountry = selectedCountry,
            isLoading = isLoading,
            countryBorders = countryBorders,
            lastCameraPosition = lastCameraPosition
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    init {
        loadData()
    }

    private fun loadData() {
        if (userId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val allCountries = getCountriesUseCase.execute(userId)
            _countries.value = allCountries
            _visitedCountryCodes.value = allCountries.filter { it.visited }.mapTo(mutableSetOf()) { it.code }
            _visitedCities.value = allCountries.associate { country ->
                country.code to country.cities.filter { it.visited }.mapTo(mutableSetOf()) { it.name }
            }

            _countryBorders.value = getCountryGeometriesUseCase.execute()
            _isLoading.value = false
        }
    }

    fun toggleCountryVisited(code: String) {
        val nowVisited = !_visitedCountryCodes.value.contains(code)

        _visitedCountryCodes.update { if (nowVisited) it + code else it - code }
        updateSelectedCountry(code, nowVisited)

        if (!nowVisited) {
            _visitedCities.update { it - code }
        }

        viewModelScope.launch {
            updateCountryVisitedUseCase.execute(userId, code, nowVisited)
        }
    }

    private fun updateSelectedCountry(code: String, visited: Boolean) {
        _selectedCountry.update {
            if (it?.code == code) it.copy(
                visited = visited,
                cities = it.cities.map { c -> c.copy(visited = if (!visited) false else c.visited) }
            ) else it
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        val nowVisited = _visitedCities.value[countryCode]?.contains(cityName) != true

        _visitedCities.update { current ->
            val updatedCities = (current[countryCode] ?: emptySet()).let {
                if (nowVisited) it + cityName else it - cityName
            }
            current + (countryCode to updatedCities)
        }

        updateCityVisitedStates(countryCode, cityName, nowVisited)

        viewModelScope.launch {
            updateCityVisitedUseCase.execute(userId, countryCode, cityName, nowVisited)

            val remainingCities = _visitedCities.value[countryCode]?.size ?: 0
            val isCountryVisited = _visitedCountryCodes.value.contains(countryCode)

            if (nowVisited && !isCountryVisited) toggleCountryVisited(countryCode)
            if (!nowVisited && remainingCities == 0 && isCountryVisited) toggleCountryVisited(countryCode)
        }
    }

    private fun updateCityVisitedStates(code: String, city: String, visited: Boolean) {
        _countries.update { list ->
            list.map { country ->
                if (country.code == code) {
                    country.copy(cities = country.cities.map {
                        if (it.name == city) it.copy(visited = visited) else it
                    })
                } else country
            }
        }

        _selectedCountry.update { current ->
            if (current?.code == code) {
                current.copy(cities = current.cities.map {
                    if (it.name == city) it.copy(visited = visited) else it
                })
            } else current
        }
    }

    fun notifyUserMovedMap(position: CameraPosition) {
        _lastCameraPosition.value = position
    }

    suspend fun resolveCountryFromLatLng(latLng: LatLng): LatLngBounds? {
        val code = withContext(Dispatchers.Default) {
            mapRepo.getCountryCodeFromLatLng(latLng)
        }

        if (code == null) {
            _selectedCountry.value = null
            return null
        }

        val fullCountry = getCountryByCode(_countries.value, code) ?: return null

        val isVisited = visitedCountryCodes.value.contains(fullCountry.code) ||
                (_visitedCities.value[fullCountry.code]?.isNotEmpty() == true)

        _selectedCountry.value = fullCountry.copy(visited = isVisited)

        return mapRepo.getCountryBounds()[code]
    }

    fun isSameCountrySelected(latLng: LatLng): Boolean {
        val code = mapRepo.getCountryCodeFromLatLng(latLng)
        return selectedCountry.value?.code?.equals(code, ignoreCase = true) == true
    }

    suspend fun getVisitedCountriesCenterAndBounds(): Pair<LatLng, LatLngBounds>? = withContext(Dispatchers.IO) {
        val codes = _visitedCountryCodes.value
        if (codes.isEmpty()) return@withContext null

        val allPoints = codes.flatMap {
            mapRepo.getCountryGeometries()[it]?.polygons?.flatten().orEmpty()
        }
        if (allPoints.isEmpty()) return@withContext null

        val bounds = LatLngBounds.builder().apply {
            allPoints.forEach { include(it) }
        }.build()

        val center = LatLng(
            allPoints.map { it.latitude }.average(),
            allPoints.map { it.longitude }.average()
        )
        center to bounds
    }

    fun setFrom(from: Screens) {
        _cameFrom.value = from
    }
}