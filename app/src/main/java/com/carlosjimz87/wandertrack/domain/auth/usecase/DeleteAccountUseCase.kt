package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository

import kotlinx.coroutines.tasks.await

open class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) {
    open suspend fun execute() {
        val user = authRepository.currentUser ?: return
        firestoreRepository.deleteUserDocument(user.uid)
        user.delete().await()
    }
}