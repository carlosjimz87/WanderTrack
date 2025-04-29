package com.carlosjimz87.wandertrack.common

import com.carlosjimz87.wandertrack.domain.models.Country

object Constants {
    val visitedCountries = listOf(
        Country("ID", "Indonesia", visited = true),
        Country("NO", "Norway", visited = true),
        Country("PH", "Philippines", visited = true),
        Country("GR", "Greece", visited = true),
        Country("HR", "Croatia", visited = true),
        Country("CA", "Canada", visited = true),
    )
}