package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.Country

interface FirestoreRepository {
    suspend fun fetchCountries(): List<Country>
    suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean)
    suspend fun updateCityVisited(userId: String, countryCode: String, cityName: String, visited: Boolean)
}