package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository

open class EnsureUserDocumentUseCase(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) {
    open suspend fun execute() {
        val userId = authRepository.currentUser?.uid ?: throw Exception("USER_NOT_FOUND")
        firestoreRepository.ensureUserDocument(userId)
        firestoreRepository.recalculateAndUpdateStats(userId)
    }
}