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
import com.carlosjimz87.wandertrack.utils.getCountryByCode
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.carlosjimz87.wandertrack.ui.screens.map.state.MapUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val userId: String,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val getCountryGeometriesUseCase: GetCountryGeometriesUseCase,
    private val updateCountryVisitedUseCase: UpdateCountryVisitedUseCase,
    private val updateCityVisitedUseCase: UpdateCityVisitedUseCase,
    private val mapRepo: MapRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
        observeVisitedUnion()
    }

    private fun loadData() {
        if (userId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val allCountries = getCountriesUseCase.execute(userId)
                val visitedCountryCodes = allCountries
                    .filter { it.visited }
                    .mapTo(mutableSetOf()) { it.code }
                val visitedCities = allCountries.associate { country ->
                    country.code to country.cities
                        .filter { it.visited }
                        .mapTo(mutableSetOf()) { it.name }
                }
                val countryBorders = getCountryGeometriesUseCase.execute()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        countries = allCountries,
                        visitedCountryCodes = visitedCountryCodes,
                        visitedCities = visitedCities,
                        countryBorders = countryBorders
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun observeVisitedUnion() = viewModelScope.launch {
        uiState
            .map { it.visitedCountryCodes to it.countryBorders }
            .distinctUntilChanged()
            .mapLatest { (codes, borders) ->
                computeVisitedUnion(codes, borders)
            }
            .collect { result ->
                _uiState.update {
                    it.copy(
                        visitedUnionCenter = result?.first,
                        visitedUnionBounds = result?.second
                    )
                }
            }
    }

    fun toggleCountryVisited(code: String) {
        val nowVisited = !_uiState.value.visitedCountryCodes.contains(code)

        // Optimistic update (single atomic update)
        _uiState.update {
            it.copy(
                visitedCountryCodes = if (nowVisited)
                    it.visitedCountryCodes + code
                else
                    it.visitedCountryCodes - code,
                selectedCountry = it.selectedCountry?.let { sc ->
                    if (sc.code == code) sc.copy(
                        visited = nowVisited,
                        cities = sc.cities.map { c -> c.copy(visited = if (!nowVisited) false else c.visited) }
                    ) else sc
                },
                visitedCities = if (!nowVisited) it.visitedCities - code else it.visitedCities
            )
        }

        // Persist
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateCountryVisitedUseCase.execute(userId, code, nowVisited)
            } catch (e: Exception) {
                // Optional: revert on failure
            }
        }
    }

    fun toggleCityVisited(countryCode: String, cityName: String) {
        val nowVisited = _uiState.value.visitedCities[countryCode]?.contains(cityName) != true

        // Optimistic update (single atomic update)
        _uiState.update { st ->
            val updatedCitiesForCountry = (st.visitedCities[countryCode] ?: emptySet()).let {
                if (nowVisited) it + cityName else it - cityName
            }
            val newVisitedCities = st.visitedCities + (countryCode to updatedCitiesForCountry)

            val newCountries = st.countries.map { c ->
                if (c.code == countryCode) {
                    c.copy(cities = c.cities.map { city ->
                        if (city.name == cityName) city.copy(visited = nowVisited) else city
                    })
                } else c
            }

            val selectedUpdated = st.selectedCountry?.let { sc ->
                if (sc.code == countryCode) {
                    sc.copy(cities = sc.cities.map { city ->
                        if (city.name == cityName) city.copy(visited = nowVisited) else city
                    })
                } else sc
            }

            // Auto-toggle country visited if needed
            val hadCountryVisited = st.visitedCountryCodes.contains(countryCode)
            val remaining = updatedCitiesForCountry.size
            val newVisitedCodes = when {
                nowVisited && !hadCountryVisited -> st.visitedCountryCodes + countryCode
                !nowVisited && remaining == 0 && hadCountryVisited -> st.visitedCountryCodes - countryCode
                else -> st.visitedCountryCodes
            }

            st.copy(
                countries = newCountries,
                visitedCities = newVisitedCities,
                visitedCountryCodes = newVisitedCodes,
                selectedCountry = selectedUpdated
            )
        }

        // Persist
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateCityVisitedUseCase.execute(userId, countryCode, cityName, nowVisited)
                // If you want strict server-sync for country toggle, call updateCountryVisitedUseCase accordingly.
            } catch (e: Exception) {
                // Optional: revert on failure
            }
        }
    }

    fun notifyUserMovedMap(position: CameraPosition) {
        _uiState.update { it.copy(lastCameraPosition = position) }
    }

    suspend fun resolveCountryFromLatLng(latLng: LatLng): LatLngBounds? {
        return withContext(Dispatchers.IO) {
            val code = mapRepo.getCountryCodeFromLatLng(latLng)

            if (code == null) {
                _uiState.update { it.copy(selectedCountry = null) }
                return@withContext null
            }

            val fullCountry = getCountryByCode(_uiState.value.countries, code) ?: return@withContext null

            val isVisited = _uiState.value.visitedCountryCodes.contains(fullCountry.code) ||
                    (_uiState.value.visitedCities[fullCountry.code]?.isNotEmpty() == true)

            _uiState.update { it.copy(selectedCountry = fullCountry.copy(visited = isVisited)) }

            mapRepo.getCountryBounds()[code]
        }
    }

    fun isSameCountrySelected(latLng: LatLng): Boolean {
        val code = mapRepo.getCountryCodeFromLatLng(latLng)
        return uiState.value.selectedCountry?.code?.equals(code, ignoreCase = true) == true
    }

    suspend fun getVisitedCountriesCenterAndBounds(): Pair<LatLng, LatLngBounds>? = withContext(Dispatchers.IO) {
        val codes = _uiState.value.visitedCountryCodes
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

    fun onMapClick(latLng: LatLng) = viewModelScope.launch(Dispatchers.Default) {
        _uiState.update { it.copy(isResolvingTap = true, errorMessage = null) }
        try {
            val code = mapRepo.getCountryCodeFromLatLng(latLng)
            if (code == null) {
                _uiState.update { it.copy(selectedCountry = null, isResolvingTap = false) }
                return@launch
            }

            val snapshot = _uiState.value
            val fullCountry = getCountryByCode(snapshot.countries, code)
            if (fullCountry == null) {
                _uiState.update { it.copy(selectedCountry = null, isResolvingTap = false) }
                return@launch
            }

            val isVisited = snapshot.visitedCountryCodes.contains(fullCountry.code) ||
                    (snapshot.visitedCities[fullCountry.code]?.isNotEmpty() == true)

            _uiState.update {
                it.copy(
                    selectedCountry = fullCountry.copy(visited = isVisited),
                    isResolvingTap = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isResolvingTap = false, errorMessage = e.message) }
        }
    }

    private suspend fun computeVisitedUnion(
        codes: Set<String>,
        borders: Map<String, CountryGeometry>
    ): Pair<LatLng, LatLngBounds>? = withContext(Dispatchers.Default) {
        if (codes.isEmpty()) return@withContext null

        val allPoints = codes.flatMap { code ->
            borders[code]?.polygons?.flatten().orEmpty()
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
}