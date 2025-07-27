package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.common.AppConfig
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.models.profile.UserVisits
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.managers.LocalDataManager
import com.carlosjimz87.wandertrack.utils.AchievementsCalculator
import com.carlosjimz87.wandertrack.utils.Logger
import com.carlosjimz87.wandertrack.utils.toProfileUiState
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl(
    private val firestoreDatabase: FirebaseFirestore,
    private val dataManager: LocalDataManager
) : FirestoreRepository {

    companion object {
        private const val USERS = "users"
        private const val USERS_DEV = "users_dev"
        private const val VISITED_COUNTRIES = "visited_countries"
        private const val VISITED_CITIES = "visited_cities"
    }

    private fun basePath(): String =
        if (AppConfig.isProd) USERS else USERS_DEV

    private fun userDoc(userId: String): DocumentReference {
        require(userId.isNotBlank()) {
            "FirestoreRepositoryImpl: userId must not be null or blank"
        }
        return firestoreDatabase.collection(basePath()).document(userId)
    }

    private fun cityDoc(userId: String, countryCode: String) =
        userDoc(userId).collection(VISITED_CITIES).document(countryCode)

    // ------------------ USER ------------------

    override suspend fun ensureUserDocument(userId: String) {
        val doc = userDoc(userId)
        if (!doc.get().await().exists()) {
            doc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        }
    }

    override suspend fun deleteUserDocument(userId: String) {
        userDoc(userId).delete().await()
    }

    // ------------------ READS ------------------

    override suspend fun fetchAllCountries(userId: String): List<Country> = withEnsuredUser(userId) {
        val visitedCountries = getVisitedCountries(userId)
        val visitedCities = getVisitedCitiesMap(userId)

        return@withEnsuredUser dataManager.preloadedCountries.map { country ->
            country.copy(
                visited = visitedCountries.contains(country.code),
                cities = country.cities.map { city ->
                    city.copy(visited = visitedCities[country.code]?.contains(city.name) == true)
                }
            )
        }
    }

    override suspend fun fetchUserVisits(userId: String): UserVisits  = withEnsuredUser(userId) {
        return@withEnsuredUser UserVisits(
            countryCodes = getVisitedCountries(userId),
            cities = getVisitedCitiesMap(userId)
        )
    }

    override suspend fun fetchUserProfile(userId: String): ProfileData? = withEnsuredUser(userId) {
        return@withEnsuredUser try {
            userDoc(userId).get().await().takeIf { it.exists() }?.toProfileUiState()
        } catch (e: Exception) {
            Logger.e("FirestoreError -> fetchUserProfile failed", e)
            null
        }
    }

    // ------------------ UPDATES ------------------

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) = withEnsuredUser(userId) {
        val countries = getVisitedCountries(userId).toMutableSet().apply {
            if (visited) add(code) else remove(code)
        }

        if (!visited) {
            cityDoc(userId, code).delete().await()
        }

        userDoc(userId).update(VISITED_COUNTRIES, countries.toList()).await()
        recalculateAndUpdateStats(userId)
    }

    override suspend fun updateCityVisited(
        userId: String,
        countryCode: String,
        cityName: String,
        visited: Boolean
    ) = withEnsuredUser(userId) {

        val doc = cityDoc(userId, countryCode)
        val current =
            (doc.get().await().get("cities") as? List<String>)?.toMutableSet() ?: mutableSetOf()

        if (visited) current.add(cityName) else current.remove(cityName)

        if (current.isEmpty()) {
            doc.delete().await()
        } else {
            doc.set(mapOf("cities" to current.toList())).await()
        }

        recalculateAndUpdateStats(userId)
    }

    // ------------------ STATS ------------------

    override suspend fun recalculateAndUpdateStats(userId: String) {
        val visitedCountries = getVisitedCountries(userId)
        val visitedCitiesCount = getVisitedCitiesCount(userId)

        // Usa los países precargados locales
        val allCountries = dataManager.preloadedCountries
        val totalCountries = allCountries.size
        val visitedContinents = allCountries
            .filter { it.code in visitedCountries }
            .map { it.continent }
            .toSet()

        val worldPercent = if (totalCountries > 0) {
            visitedCountries.size * 100 / totalCountries
        } else 0

        val achievements = AchievementsCalculator
            .calculateAchievements(
                countriesVisited = visitedCountries.size,
                citiesVisited = visitedCitiesCount,
                continentsVisited = visitedContinents.size,
                worldPercent = worldPercent
            )
            .map { mapOf("title" to it.title, "desc" to it.description) }

        try {
            userDoc(userId).set(
                mapOf(
                    "countries" to visitedCountries.size,
                    "cities" to visitedCitiesCount,
                    "continents" to visitedContinents.size,
                    "world" to worldPercent,
                    "achievements" to achievements
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Logger.e("FirestoreRepo -> ❌Failed to update stats", e)
        }
    }

    // ------------------ INTERNAL HELPERS ------------------

    private suspend fun getVisitedCountries(userId: String): Set<String> {
        val doc = userDoc(userId).get().await()
        return (doc.get(VISITED_COUNTRIES) as? List<String>)?.toSet() ?: emptySet()
    }

    private suspend fun getVisitedCitiesMap(userId: String): Map<String, Set<String>> {
        return userDoc(userId).collection(VISITED_CITIES).get().await().documents.associate { doc ->
            val cities = doc.get("cities") as? List<String> ?: emptyList()
            doc.id to cities.toSet()
        }
    }

    private suspend fun getVisitedCitiesCount(userId: String): Int {
        return userDoc(userId).collection(VISITED_CITIES).get().await()
            .sumOf { (it.get("cities") as? List<*>)?.size ?: 0 }
    }

    private suspend inline fun <T> withEnsuredUser(userId: String, crossinline block: suspend () -> T): T {
        ensureUserDocument(userId)
        return block()
    }
}