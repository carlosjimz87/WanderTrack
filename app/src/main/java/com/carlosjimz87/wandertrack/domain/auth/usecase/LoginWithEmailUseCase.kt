package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository

open class LoginWithEmailUseCase(private val authRepository: AuthRepository) {
    open suspend fun execute(email: String, password: String): Result<Unit> {
        return authRepository.loginWithEmail(email, password)
    }
}