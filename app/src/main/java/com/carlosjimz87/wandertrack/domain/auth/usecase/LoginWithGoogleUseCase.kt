package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository

open class LoginWithGoogleUseCase(private val authRepository: AuthRepository) {
    open suspend fun execute(idToken: String): Result<Unit> {
        return authRepository.loginWithGoogle(idToken)
    }
}