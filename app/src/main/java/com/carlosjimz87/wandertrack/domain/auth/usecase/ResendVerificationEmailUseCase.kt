package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository

open class ResendVerificationEmailUseCase(private val authRepository: AuthRepository) {
    open suspend fun execute(): Result<String> {
        return authRepository.resendVerificationEmail()
    }
}