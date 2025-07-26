package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.models.profile.UserVisits
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.utils.AchievementsCalculator
import com.carlosjimz87.wandertrack.utils.Logger
import com.carlosjimz87.wandertrack.utils.toProfileUiState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl(
    private val db: FirebaseFirestore
) : FirestoreRepository {

    companion object {
        private const val USERS = "users_dev"
        private const val USERS_DEV = "users_dev"
        private const val META = "meta"
        private const val COUNTRIES = "countries"
        private const val VISITED_COUNTRIES = "visited_countries"
        private const val VISITED_CITIES = "visited_cities"
    }

    private fun userBasePath(): String =
        if (BuildConfig.FIREBASE_ENV == "prod") USERS else USERS_DEV

    private fun visitedCountriesCol(userId: String): CollectionReference =
        db.collection(userBasePath()).document(userId).collection(VISITED_COUNTRIES)

    private fun visitedCitiesDoc(userId: String, countryCode: String): DocumentReference =
        db.collection(userBasePath()).document(userId).collection(VISITED_CITIES).document(countryCode)

    override suspend fun fetchAllCountries(userId: String): List<Country> {
        return try {
            val metaSnapshot = db.collection(META).document(COUNTRIES)
                .collection("all").get().await()

            val visitedCountries = try {
                visitedCountriesCol(userId).get().await().documents.map { it.id }.toSet()
            } catch (e: Exception) {
                Logger.e("FirestoreError -> visitedCountriesCol fetch failed: ${e.message}", e)
                emptySet()
            }

            val visitedCitiesMap = try {
                db.collection(userBasePath()).document(userId)
                    .collection(VISITED_CITIES).get().await()
                    .documents.associate { doc ->
                        val code = doc.id
                        val cities = doc.get("cities") as? List<String> ?: emptyList()
                        code to cities.toSet()
                    }
            } catch (e: Exception) {
                Logger.e("FirestoreError -> visitedCitiesMap fetch failed: ${e.message}", e)
                emptyMap()
            }

            val countries = metaSnapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val code = doc.getString("code") ?: return@mapNotNull null
                val citiesList = doc.get("cities") as? List<Map<String, Any>> ?: emptyList()

                val visited = visitedCountries.contains(code)

                val cities = citiesList.mapNotNull { cityMap ->
                    val cityName = cityMap["name"] as? String ?: return@mapNotNull null
                    val lat = (cityMap["latitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                    val lng = (cityMap["longitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                    val visitedCity = visitedCitiesMap[code]?.contains(cityName) == true
                    City(name = cityName, latitude = lat, longitude = lng, visited = visitedCity)
                }

                Country(code = code, name = name, visited = visited, cities = cities)
            }

            Logger.w("FirestoreDebug -> ✅ Fetched ${countries.size} countries for $userId")
            countries
        } catch (e: Exception) {
            Logger.e("FirestoreError -> fetchAllCountries failed: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun fetchUserVisits(userId: String): UserVisits {
        Logger.d("FirestoreDebug -> 📌 fetchUserVisits for userId=$userId")
        Logger.d("FirestoreDebug -> 🔗 Current firestore base path: ${userBasePath()}")
        return try {
            val visitedCountryCodes = visitedCountriesCol(userId)
                .get().await()
                .documents.map { it.id }.toSet()

            val visitedCitiesDocs = db.collection(userBasePath())
                .document(userId).collection(VISITED_CITIES).get().await()

            val visitedCitiesMap = visitedCitiesDocs.documents.associate { doc ->
                val code = doc.id
                val cities = doc.get("cities") as? List<String> ?: emptyList()
                code to cities.toSet()
            }

            UserVisits(
                countryCodes = visitedCountryCodes,
                cities = visitedCitiesMap
            )
        } catch (e: Exception) {
            Logger.e("FirestoreError -> fetchUserVisits failed: ${e.message}", e)
            UserVisits()
        }
    }

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) {
        Logger.d("FirestoreDebug -> 🗺️ updateCountryVisited userId=$userId, code=$code, visited=$visited")
        val docRef = visitedCountriesCol(userId).document(code)
        val citiesDocRef = visitedCitiesDoc(userId, code)

        try {
            if (visited) {
                docRef.set(mapOf("visited" to true)).await()
            } else {
                docRef.delete().await()
                citiesDocRef.delete().await()
            }

            recalculateAndUpdateStats(userId)
        } catch (e: Exception) {
            Logger.e("FirestoreError -> updateCountryVisited failed: ${e.message}", e)
        }
    }

    override suspend fun updateCityVisited(
        userId: String,
        countryCode: String,
        cityName: String,
        visited: Boolean
    ) {
        Logger.d("FirestoreDebug -> 🏙️ updateCityVisited userId=$userId, $countryCode -> $cityName = $visited")
        val docRef = visitedCitiesDoc(userId, countryCode)

        try {
            val snapshot = docRef.get().await()
            val cities = (snapshot.get("cities") as? List<String>)?.toMutableSet() ?: mutableSetOf()

            if (visited) {
                cities.add(cityName)
            } else {
                cities.remove(cityName)
            }

            if (cities.isEmpty()) {
                docRef.delete().await()
            } else {
                docRef.set(mapOf("cities" to cities.toList())).await()
            }

            recalculateAndUpdateStats(userId)
        } catch (e: Exception) {
            Logger.e("FirestoreError -> updateCityVisited failed: ${e.message}", e)
        }
    }

    override suspend fun deleteUserDocument(userId: String) {
        Logger.d("FirestoreDebug -> ❌ deleteUserDocument for userId=$userId")
        try {
            db.collection(userBasePath()).document(userId).delete().await()
        } catch (e: Exception) {
            Logger.e("FirestoreError -> deleteUserDocument failed: ${e.message}", e)
        }
    }

    override suspend fun ensureUserDocument(userId: String) {
        Logger.d("FirestoreDebug -> 🛠️ ensureUserDocument for userId=$userId")
        try {
            val userDoc = db.collection(userBasePath()).document(userId)
            val snapshot = userDoc.get().await()

            if (!snapshot.exists()) {
                userDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
                Logger.w("FirestoreDebug -> ✅ Created user document for $userId")
            }
        } catch (e: Exception) {
            Logger.e("FirestoreError -> ensureUserDocument failed: ${e.message}", e)
        }
    }

    override suspend fun fetchUserProfile(userId: String): ProfileData? {
        Logger.d("FirestoreDebug -> 🙍 fetchUserProfile for userId=$userId")
        return try {
            val doc = db.collection(userBasePath()).document(userId).get().await()
            if (!doc.exists()) return null
            doc.toProfileUiState()
        } catch (e: Exception) {
            Logger.e("FirestoreError -> fetchUserProfile failed: ${e.message}", e)
            null
        }
    }

    override suspend fun recalculateAndUpdateStats(userId: String) {
        Logger.d("FirestoreDebug -> 📊 recalculateAndUpdateStats for userId=$userId")
        try {
            val visitedCountriesSnapshot = visitedCountriesCol(userId).get().await()
            val countriesVisited = visitedCountriesSnapshot.size()

            val visitedCitiesSnapshot = db.collection(userBasePath())
                .document(userId).collection(VISITED_CITIES).get().await()

            val totalCitiesVisited = visitedCitiesSnapshot.documents.sumOf { doc ->
                val cities = doc.get("cities") as? List<String> ?: emptyList()
                cities.size
            }

            val metaSnapshot = db.collection(META).document(COUNTRIES).collection("all").get().await()
            val visitedCountryCodes = visitedCountriesSnapshot.documents.map { it.id }.toSet()
            val totalCountries = metaSnapshot.size()
            val visitedContinents = mutableSetOf<String>()

            for (doc in metaSnapshot.documents) {
                val code = doc.getString("code") ?: continue
                if (visitedCountryCodes.contains(code)) {
                    val continent = doc.getString("continent") ?: continue
                    visitedContinents.add(continent)
                }
            }

            val continentsVisited = visitedContinents.size
            val worldPercent = if (totalCountries > 0) {
                (visitedCountryCodes.size * 100) / totalCountries
            } else 0

            val achievements = AchievementsCalculator.calculateAchievements(
                countriesVisited = countriesVisited,
                citiesVisited = totalCitiesVisited,
                continentsVisited = continentsVisited,
                worldPercent = worldPercent
            ).map {
                mapOf("title" to it.title, "desc" to it.description)
            }

            db.collection(userBasePath()).document(userId).update(
                mapOf(
                    "countries" to countriesVisited,
                    "cities" to totalCitiesVisited,
                    "continents" to continentsVisited,
                    "world" to worldPercent,
                    "achievements" to achievements
                )
            ).await()
        } catch (e: Exception) {
            Logger.e("FirestoreError -> recalculateAndUpdateStats failed: ${e.message}", e)
        }
    }
}