package com.carlosjimz87.wandertrack.common

import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country

object Constants {

    const val ANIMATION_DURATION = 600

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
            visited = false,
            cities = listOf(
                City(name = "Rome", latitude = 41.9028, longitude = 12.4964),
                City(name = "Venice", latitude = 45.4, longitude = 12.3)
            )
        ),
        Country(
            code = "NO",
            name = "Norway",
            visited = false,
            cities = listOf(
                City(name = "Oslo", latitude = 59.9139, longitude = 10.7522),
                City(name = "Bergen", latitude = 60.3913, longitude = 5.3221)
            )
        ),
        Country(
            code = "PH",
            name = "Philippines",
            visited = false,
            cities = listOf(
                City(name = "Manila", latitude = 14.5995, longitude = 120.9842),
                City(name = "Cebu City", latitude = 10.3157, longitude = 123.8854)
            )
        ),
        Country(
            code = "GR",
            name = "Greece",
            visited = true,
            cities = listOf(
                City(name = "Athens", latitude = 37.9838, longitude = 23.7275),
                City(name = "Thessaloniki", latitude = 40.6401, longitude = 22.9444)
            )
        ),
        Country(
            code = "HR",
            name = "Croatia",
            visited = true,
            cities = listOf(
                City(name = "Zagreb", latitude = 45.8150, longitude = 15.9819),
                City(name = "Dubrovnik", latitude = 42.6507, longitude = 18.0944)
            )
        ),
        Country(
            code = "CA",
            name = "Canada",
            visited = false,
            cities = listOf(
                City(name = "Toronto", latitude = 43.6532, longitude = -79.3832),
                City(name = "Vancouver", latitude = 49.2827, longitude = -123.1207)
            )
        ),
        Country(
            code = "DE",
            name = "Germany",
            visited = false,
            cities = listOf(
                City(name = "Berlin", latitude = 52.5200, longitude = 13.4050),
                City(name = "Munich", latitude = 48.1351, longitude = 11.5820)
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