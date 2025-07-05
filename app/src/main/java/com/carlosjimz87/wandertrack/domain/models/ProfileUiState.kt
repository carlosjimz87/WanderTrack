package com.carlosjimz87.wandertrack.domain.models

data class ProfileUiState(
    val username: String,
    val countriesVisited: Int,
    val citiesVisited: Int,
    val continentsVisited: Int,
    val worldPercent: Int,
    val achievements: List<Achievement>
)