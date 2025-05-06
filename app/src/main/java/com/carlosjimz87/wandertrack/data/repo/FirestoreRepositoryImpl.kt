import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl : FirestoreRepository {

    private val db = Firebase.firestore

    override suspend fun fetchCountries(): List<Country> {
        return try {
            val snapshot = db.collection("meta").document("countries")
                .collection("all").get().await()

            snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val code = doc.getString("code") ?: return@mapNotNull null
                val visited = doc.getBoolean("visited") ?: false
                val citiesList = doc.get("cities") as? List<Map<String, Any>> ?: emptyList()

                val cities = citiesList.mapNotNull { cityMap ->
                    val cityName = cityMap["name"] as? String ?: return@mapNotNull null
                    val lat = (cityMap["latitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                    val lng = (cityMap["longitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                    val cityVisited = cityMap["visited"] as? Boolean ?: false
                    City(name = cityName, latitude = lat, longitude = lng, visited = cityVisited)
                }

                Country(code = code, name = name, visited = visited, cities = cities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateCountryVisited(userId: String, code: String, visited: Boolean) {
        try {
            db.collection("users")
                .document(userId)
                .collection("visitedCountries")
                .document(code)
                .set(mapOf("visited" to visited))
                .await()
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
        try {
            val docId = "${countryCode}_$cityName".replace(" ", "_")
            db.collection("users")
                .document(userId)
                .collection("visitedCities")
                .document(docId)
                .set(
                    mapOf(
                        "countryCode" to countryCode,
                        "cityName" to cityName,
                        "visited" to visited
                    )
                ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}