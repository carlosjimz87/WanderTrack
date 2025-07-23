package com.carlosjimz87.wandertrack.domain.repo

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.models.profile.UserVisits

interface FirestoreRepository {
    suspend fun fetchAllCountries(userId: String): List<Country>           // from meta
    suspend fun fetchUserVisits(userId: String): UserVisits  // visited countries + cities
    suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean)
    suspend fun updateCityVisited(userId: String, countryCode: String, cityName: String, visited: Boolean)
    suspend fun ensureUserDocument(userId: String)
    suspend fun fetchUserProfile(userId: String) : ProfileData?
    suspend fun deleteUserDocument(userId: String)
    suspend fun recalculateAndUpdateStats(userId: String)
}