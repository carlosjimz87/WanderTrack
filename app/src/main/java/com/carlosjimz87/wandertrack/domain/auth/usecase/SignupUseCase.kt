package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository

open class SignupUseCase(private val authRepository: AuthRepository) {
    open suspend fun execute(email: String, password: String): Result<String> {
        return authRepository.signup(email, password)
    }
}