package com.carlosjimz87.wandertrack.domain.models

data class ProfileData(
    val username: String,
    val countriesVisited: Int,
    val worldPercent: Int,
    val citiesVisited: Int,
    val continentsVisited: Int,
    val achievements: List<Achievement>
)
