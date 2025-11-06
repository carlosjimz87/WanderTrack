package com.carlosjimz87.wandertrack.domain.usecase

import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateCountryVisitedUseCase(private val firestoreRepo: FirestoreRepository) {
    suspend fun execute(userId: String, countryCode: String, visited: Boolean) = withContext(Dispatchers.IO) {
        firestoreRepo.updateCountryVisited(userId, countryCode, visited)
    }
}