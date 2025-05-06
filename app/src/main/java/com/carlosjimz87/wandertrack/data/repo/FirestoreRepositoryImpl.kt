import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.models.Country
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl : FirestoreRepository {

    private val db = Firebase.firestore

    override suspend fun fetchCountries(): List<Country> {
        return try {
            val snapshot = db.collection("countries").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Country::class.java)
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