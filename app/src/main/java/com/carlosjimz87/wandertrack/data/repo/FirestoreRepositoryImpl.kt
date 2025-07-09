import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.ProfileData
import com.carlosjimz87.wandertrack.domain.models.UserVisits
import com.carlosjimz87.wandertrack.utils.AchievementsCalculator
import com.carlosjimz87.wandertrack.utils.Logger
import com.carlosjimz87.wandertrack.utils.toProfileUiState
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl(
    private val db: FirebaseFirestore = Firebase.firestore
) : FirestoreRepository {

    companion object {
        private const val USERS = "users"
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
                e.printStackTrace()
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
                e.printStackTrace()
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

            Logger.w("Fetched ${countries.size} countries for $userId")
            countries
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun fetchUserVisits(userId: String): UserVisits {
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
            e.printStackTrace()
            UserVisits()
        }
    }

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) {
        val docRef = visitedCountriesCol(userId).document(code)
        try {
            if (visited) {
                docRef.set(mapOf("visited" to true)).await()
            } else {
                docRef.delete().await()
            }
            recalculateAndUpdateStats(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateCityVisited(
        userId: String,
        countryCode: String,
        cityName: String,
        visited: Boolean
    ) {
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
            e.printStackTrace()
        }
    }

    override suspend fun ensureUserDocument(userId: String) {
        try {
            val userDoc = db.collection(userBasePath()).document(userId)
            val snapshot = userDoc.get().await()

            if (!snapshot.exists()) {
                userDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
                Logger.w("Created user document for $userId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchUserProfile(userId: String): ProfileData? {
        return try {
            val doc = db.collection(userBasePath()).document(userId).get().await()

            if (!doc.exists()) return null

            return doc.toProfileUiState()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun recalculateAndUpdateStats(userId: String) {
        try {
            // Fetch visited countries subcollection
            val visitedCountriesSnapshot = visitedCountriesCol(userId)
                .get().await()

            val countriesVisited = visitedCountriesSnapshot.size()

            // Fetch visited cities subcollection
            val visitedCitiesSnapshot = db.collection(userBasePath())
                .document(userId).collection(VISITED_CITIES).get().await()

            val totalCitiesVisited = visitedCitiesSnapshot.documents.sumOf { doc ->
                val cities = doc.get("cities") as? List<String> ?: emptyList()
                cities.size
            }

            // Fetch meta to calculate continents and world percent (optional but recommended)
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

            // Calculate achievements using your isolated helper
            val achievements = AchievementsCalculator.calculateAchievements(
                countriesVisited = countriesVisited,
                citiesVisited = totalCitiesVisited,
                continentsVisited = continentsVisited,
                worldPercent = worldPercent
            ).map {
                mapOf("title" to it.title, "desc" to it.description)
            }

            // Update user document with new stats and achievements
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
            e.printStackTrace()
        }
    }
}