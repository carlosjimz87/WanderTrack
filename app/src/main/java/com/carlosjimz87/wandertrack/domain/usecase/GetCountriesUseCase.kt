package com.carlosjimz87.wandertrack.domain.usecase

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCountriesUseCase(private val firestoreRepo: FirestoreRepository) {
    suspend fun execute(userId: String): List<Country> = withContext(Dispatchers.IO) {
        firestoreRepo.ensureUserDocument(userId)
        firestoreRepo.fetchAllCountries(userId)
    }
}