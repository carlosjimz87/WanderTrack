package com.carlosjimz87.wandertrack.domain.auth.usecase

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository

open class LogoutUseCase(private val authRepository: AuthRepository) {
    open fun execute() {
        authRepository.logout()
    }
}