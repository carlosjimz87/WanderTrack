package com.carlosjimz87.wandertrack.common

import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country

object Constants {

    const val ANIMATION_DURATION = 600
    const val MIN_ZOOM_LEVEL = 2f
    const val MAX_ZOOM_LEVEL = 21f
    const val USER_PREFS_KEY = "user_prefs"
    const val LAST_SCREEN_KEY = "last_screen"

    val countries = listOf(
        Country(
            code = "ID",
            name = "Indonesia",
            visited = false,
            cities = listOf(
                City(name = "Jakarta", latitude = -6.2088, longitude = 106.8456),
                City(name = "Bali", latitude = -8.4095, longitude = 115.1889)
            )
        ),
        Country(
            code = "IT",
            name = "Italy",
            visited = true,
            cities = listOf(
                City(name = "Rome", latitude = 41.9028, longitude = 12.4964, visited = true),
                City(name = "Venice", latitude = 45.4, longitude = 12.3)
            )
        )
    )

    val countryNameToIso2 = mapOf(
        "FRANCE" to "FR",
        "UNITED STATES" to "US",
        "GERMANY" to "DE",
        "SPAIN" to "ES",
        "ITALY" to "IT",
    )
}