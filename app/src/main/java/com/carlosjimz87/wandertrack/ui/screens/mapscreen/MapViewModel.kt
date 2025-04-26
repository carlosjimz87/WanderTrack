package com.carlosjimz87.wandertrack.ui.screens.mapscreen

import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.domain.models.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {

    private val _visitedCountries = MutableStateFlow(mockVisitedCountries)
    val visitedCountries = _visitedCountries.asStateFlow()

    companion object {
        private val mockVisitedCountries = listOf(
            Country("GE","Germany", visited = true),
            Country("US","United States", visited = true),
            Country("ES","Spain", visited = true),
            Country("FR","France", visited = true),
            Country("IT","Italy", visited = true)
        )
    }
}