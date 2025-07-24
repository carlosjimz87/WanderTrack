package com.carlosjimz87.wandertrack.data.repo.fakes

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.Achievement
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.models.profile.UserVisits
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.utils.AchievementsCalculator

class FakeFirestoreRepositoryImpl : FirestoreRepository {

    private val _metaCountries = mutableListOf<Country>()
    private val _userVisitedCountries =
        mutableMapOf<String, MutableSet<String>>()  // userId -> country codes
    private val _userVisitedCities =
        mutableMapOf<String, MutableMap<String, MutableSet<String>>>() // userId -> countryCode -> city names
    private val _userStats = mutableMapOf<String, ProfileData>()  // userId -> stats/profile

    fun setInitialData(metaCountries: List<Country>) {
        _metaCountries.clear()
        _metaCountries.addAll(metaCountries.map {
            it.copy(
                visited = false,
                cities = it.cities.map { city -> city.copy(visited = false) })
        })
        _userVisitedCountries.clear()
        _userVisitedCities.clear()
        _userStats.clear()
    }

    private val fakeProfiles = mutableMapOf<String, ProfileData>()

    override suspend fun fetchAllCountries(userId: String): List<Country> {
        val visitedCountries = _userVisitedCountries[userId] ?: emptySet()
        val visitedCitiesMap = _userVisitedCities[userId] ?: emptyMap()

        return _metaCountries.map { base ->
            val visited = visitedCountries.contains(base.code)
            val updatedCities = base.cities.map { city ->
                val isVisited = visitedCitiesMap[base.code]?.contains(city.name) ?: false
                city.copy(visited = isVisited)
            }
            base.copy(visited = visited, cities = updatedCities)
        }
    }

    override suspend fun fetchUserVisits(userId: String): UserVisits {
        return UserVisits(
            countryCodes = _userVisitedCountries[userId] ?: emptySet(),
            cities = _userVisitedCities[userId] ?: emptyMap()
        )
    }

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) {
        val set = _userVisitedCountries.getOrPut(userId) { mutableSetOf() }
        if (visited) set.add(code) else set.remove(code)
        recalculateAndUpdateStats(userId)
    }

    override suspend fun updateCityVisited(
        userId: String,
        countryCode: String,
        cityName: String,
        visited: Boolean
    ) {
        val userCities = _userVisitedCities.getOrPut(userId) { mutableMapOf() }
        val countryCities = userCities.getOrPut(countryCode) { mutableSetOf() }

        if (visited) {
            countryCities.add(cityName)
        } else {
            countryCities.remove(cityName)
        }

        if (countryCities.isEmpty()) {
            userCities.remove(countryCode)
        }

        recalculateAndUpdateStats(userId)
    }

    override suspend fun ensureUserDocument(userId: String) {
        _userVisitedCountries.putIfAbsent(userId, mutableSetOf())
        _userVisitedCities.putIfAbsent(userId, mutableMapOf())
    }

    fun setFakeProfile(userId: String, profile: ProfileData) {
        fakeProfiles[userId] = profile
    }

    override suspend fun fetchUserProfile(userId: String): ProfileData? {
        return fakeProfiles[userId] ?: _userStats[userId]
    }

    override suspend fun deleteUserDocument(userId: String) {
        _userVisitedCountries.remove(userId)
        _userVisitedCities.remove(userId)
        _userStats.remove(userId)
    }

    override suspend fun recalculateAndUpdateStats(userId: String) {
        val visitedCountries = _userVisitedCountries[userId] ?: emptySet()
        val visitedCitiesMap = _userVisitedCities[userId] ?: emptyMap()

        val countriesVisited = visitedCountries.size
        val totalCitiesVisited = visitedCitiesMap.values.sumOf { it.size }

        val visitedContinents = _metaCountries
            .filter { visitedCountries.contains(it.code) }
            .mapNotNull { getContinentForCode(it.code) }
            .toSet()

        val continentsVisited = visitedContinents.size
        val totalCountries = _metaCountries.size
        val worldPercent = if (totalCountries > 0) (countriesVisited * 100) / totalCountries else 0

        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = countriesVisited,
            citiesVisited = totalCitiesVisited,
            continentsVisited = continentsVisited,
            worldPercent = worldPercent
        ).map { Achievement(it.title, it.description) }

        _userStats[userId] = ProfileData(
            username = "Test User",
            countriesVisited = countriesVisited,
            citiesVisited = totalCitiesVisited,
            continentsVisited = continentsVisited,
            worldPercent = worldPercent,
            achievements = achievements
        )
    }

    // Simple dummy continent mapping for tests
    private fun getContinentForCode(code: String): String? {
        return when (code) {
            "IT" -> "Europe"
            "BR" -> "South America"
            "US" -> "North America"
            else -> null
        }
    }
}