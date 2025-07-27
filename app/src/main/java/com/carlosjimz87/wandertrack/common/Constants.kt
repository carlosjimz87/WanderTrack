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
            continent = "Asia",
            cities = listOf(
                City(name = "Jakarta", latitude = -6.2088, longitude = 106.8456),
                City(name = "Bali", latitude = -8.4095, longitude = 115.1889),
                City(name = "Yogyakarta", latitude = -7.7956, longitude = 110.3695),
                City(name = "Surabaya", latitude = -7.2756, longitude = 112.6410),
                City(name = "Bandung", latitude = -6.9175, longitude = 107.6191),
                City(name = "Medan", latitude = 3.5952, longitude = 98.6722)
            )
        ),
        Country(
            code = "IT",
            name = "Italy",
            visited = true,
            continent = "Europe",
            cities = listOf(
                City(name = "Rome", latitude = 41.9028, longitude = 12.4964, visited = true),
                City(name = "Venice", latitude = 45.4, longitude = 12.3, visited = true),
                City(name = "Florence", latitude = 43.7696, longitude = 11.2558, visited = true),
                City(name = "Milan", latitude = 45.4642, longitude = 9.1900),
                City(name = "Naples", latitude = 40.8518, longitude = 14.2681),
                City(name = "Turin", latitude = 45.0703, longitude = 7.6869)
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