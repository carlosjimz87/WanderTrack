package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.domain.models.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {

    private val _countries = MutableStateFlow(mockVisitedCountries)
    val countries = _countries.asStateFlow()

    companion object {
        private val mockVisitedCountries = listOf(
            Country("GE","Germany", visited = true),
            Country("US","United States", visited = true),
            Country("ES","Spain", visited = true),
            Country("FR","France", visited = true),
            Country("IT","Italy", visited = true)
        )
    }

    fun getCountryByCode(code: String): Country? {
        return countries.value.find { it.code == code }
    }

    fun toggleCountryVisited(code: String) {
        _countries.update { list ->
            list.map {
                if (it.code == code) it.copy(visited = !it.visited) else it
            }
        }
    }
}