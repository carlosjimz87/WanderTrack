package com.carlosjimz87.wandertrack.domain.usecase

import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateCityVisitedUseCase(private val firestoreRepo: FirestoreRepository) {
    suspend fun execute(userId: String, countryCode: String, cityName: String, visited: Boolean) = withContext(Dispatchers.IO) {
        firestoreRepo.updateCityVisited(userId, countryCode, cityName, visited)
    }
}