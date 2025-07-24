package com.carlosjimz87.wandertrack.domain.models.profile

data class ProfileData(
    val username: String = "",
    val avatarUrl: String? = null,
    val countriesVisited: Int = 0,
    val citiesVisited: Int = 0,
    val continentsVisited: Int = 0,
    val worldPercent: Int = 0,
    val achievements: List<Achievement> = emptyList()
)