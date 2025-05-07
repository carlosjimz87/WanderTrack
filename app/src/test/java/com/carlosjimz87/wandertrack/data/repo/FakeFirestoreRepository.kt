package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.UserVisits

class FakeFirestoreRepository : FirestoreRepository {

    private val _metaCountries = mutableListOf<Country>()
    private val _userVisitedCountries = mutableMapOf<String, MutableSet<String>>()  // userId -> country codes
    private val _userVisitedCities = mutableMapOf<String, MutableMap<String, MutableSet<String>>>() // userId -> countryCode -> city names

    fun setInitialData(metaCountries: List<Country>) {
        _metaCountries.clear()
        _metaCountries.addAll(metaCountries.map { it.copy(visited = false, cities = it.cities.map { city -> city.copy(visited = false) }) })
        _userVisitedCountries.clear()
        _userVisitedCities.clear()
    }

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
    }

    override suspend fun updateCityVisited(userId: String, countryCode: String, cityName: String, visited: Boolean) {
        val countryCities = _userVisitedCities.getOrPut(userId) { mutableMapOf() }
            .getOrPut(countryCode) { mutableSetOf() }

        if (visited) {
            countryCities.add(cityName)
        } else {
            countryCities.remove(cityName)
        }

        // Clean up empty entries
        if (countryCities.isEmpty()) {
            _userVisitedCities[userId]?.remove(countryCode)
        }
    }
}