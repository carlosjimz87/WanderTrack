package com.carlosjimz87.wandertrack.fakes

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.Achievement
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.models.profile.UserVisits
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository

class FakeFirestoreRepositoryImpl(
    private val achievementsCalc: (Int, Int, Int, Int) -> List<Achievement> = { c, ci, co, w -> emptyList() }
) : FirestoreRepository {

    // Flags de fallo opcional
    var shouldFailFetch = false
    var shouldFailUpdate = false
    var shouldFailDelete = false

    private val metaCountries = mutableListOf<Country>()
    private val visitedCountries =
        mutableMapOf<String, MutableSet<String>>()              // uid -> set<code>
    private val visitedCities =
        mutableMapOf<String, MutableMap<String, MutableSet<String>>>() // uid -> code -> set<city>
    private val stats =
        mutableMapOf<String, ProfileData>()                                // uid -> stats
    private val fakeProfiles = mutableMapOf<String, ProfileData>()

    fun setInitialData(meta: List<Country>) {
        metaCountries.clear()
        metaCountries += meta.map {
            it.copy(
                visited = false,
                cities = it.cities.map { c -> c.copy(visited = false) })
        }
        visitedCountries.clear(); visitedCities.clear(); stats.clear(); fakeProfiles.clear()
    }

    suspend fun seedVisited(
        userId: String,
        countries: Set<String> = emptySet(),
        cities: Map<String, Set<String>> = emptyMap()
    ) {
        visitedCountries[userId] = countries.toMutableSet()
        visitedCities[userId] = cities.mapValues { it.value.toMutableSet() }.toMutableMap()
        recalculateAndUpdateStats(userId)
    }

    override suspend fun ensureUserDocument(userId: String) {
        visitedCountries.putIfAbsent(userId, mutableSetOf())
        visitedCities.putIfAbsent(userId, mutableMapOf())
    }

    override suspend fun fetchAllCountries(userId: String): List<Country> {
        if (shouldFailFetch) throw IllegalStateException("fetchAll failed")
        val vCountries = visitedCountries[userId].orEmpty()
        val vCities = visitedCities[userId].orEmpty()
        // devolver COPIAS
        return metaCountries.map { base ->
            val visited = base.code in vCountries
            val cities = base.cities.map { c ->
                c.copy(visited = vCities[base.code]?.contains(c.name) == true)
            }
            base.copy(visited = visited, cities = cities)
        }.toList()
    }

    override suspend fun fetchUserVisits(userId: String): UserVisits {
        if (shouldFailFetch) throw IllegalStateException("fetchVisits failed")
        return UserVisits(
            countryCodes = visitedCountries[userId]?.toSet() ?: emptySet(),
            cities = visitedCities[userId]?.mapValues { it.value.toSet() } ?: emptyMap()
        )
    }

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) {
        if (shouldFailUpdate) throw IllegalStateException("updateCountry failed")
        val set = visitedCountries.getOrPut(userId) { mutableSetOf() }
        if (visited) set.add(code) else set.remove(code)
        recalculateAndUpdateStats(userId)
    }

    override suspend fun updateCityVisited(
        userId: String,
        countryCode: String,
        cityName: String,
        visited: Boolean
    ) {
        if (shouldFailUpdate) throw IllegalStateException("updateCity failed")
        val userMap = visitedCities.getOrPut(userId) { mutableMapOf() }
        val set = userMap.getOrPut(countryCode) { mutableSetOf() }
        if (visited) set.add(cityName) else set.remove(cityName)
        if (set.isEmpty()) userMap.remove(countryCode)
        recalculateAndUpdateStats(userId)
    }

    override suspend fun fetchUserProfile(userId: String): ProfileData? {
        if (shouldFailFetch) throw IllegalStateException("fetchProfile failed")
        return fakeProfiles[userId] ?: stats[userId]
    }

    fun setFakeProfile(userId: String, profile: ProfileData) {
        fakeProfiles[userId] = profile
    }

    override suspend fun deleteUserDocument(userId: String) {
        if (shouldFailDelete) throw IllegalStateException("delete failed")
        visitedCountries.remove(userId); visitedCities.remove(userId); stats.remove(userId); fakeProfiles.remove(
            userId
        )
    }

    override suspend fun recalculateAndUpdateStats(userId: String) {
        val cVisitedAll = visitedCountries[userId].orEmpty()
        val citiesVisitedAll = visitedCities[userId].orEmpty().values.sumOf { it.size }

        // Only count visits that are present in metaCountries
        val metaCodes = metaCountries.map { it.code }.toSet()
        val cVisitedValid = cVisitedAll.intersect(metaCodes)

        val continentsVisited = metaCountries
            .filter { it.code in cVisitedValid }
            .mapNotNull { getContinentForCode(it.code) }
            .toSet()
            .size

        val totalCountries = metaCountries.size.coerceAtLeast(1)
        val worldPercent = (cVisitedValid.size * 100) / totalCountries

        val ach = achievementsCalc(
            cVisitedValid.size,
            citiesVisitedAll,          // keep as-is; or filter by meta if you prefer
            continentsVisited,
            worldPercent
        )

        stats[userId] = ProfileData(
            username = "Test User",
            countriesVisited = cVisitedValid.size,
            citiesVisited = citiesVisitedAll,
            continentsVisited = continentsVisited,
            worldPercent = worldPercent,
            achievements = ach
        )
    }

    private fun getContinentForCode(code: String): String? = when (code) {
        "IT" -> "Europe"; "BR" -> "South America"; "US" -> "North America"; else -> null
    }
}