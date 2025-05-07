package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.UserVisits

interface FirestoreRepository {
    suspend fun fetchAllCountries(userId: String): List<Country>           // from meta
    suspend fun fetchUserVisits(userId: String): UserVisits  // visited countries + cities
    suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean)
    suspend fun updateCityVisited(userId: String, countryCode: String, cityName: String, visited: Boolean)
}