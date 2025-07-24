package com.carlosjimz87.wandertrack.managers

import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class SessionManagerImpl(
    private val authRepository: AuthRepository
) : SessionManager {

    private val _validSession = MutableStateFlow<Boolean?>(null)
    override val validSession: StateFlow<Boolean?> get() = _validSession.asStateFlow()

    init {
        _validSession.value = authRepository.isUserLoggedIn()

        authRepository.addAuthStateListener {
            _validSession.value = authRepository.isUserLoggedIn()
        }
    }

    override fun refreshSession() {
        _validSession.value = authRepository.isUserLoggedIn()
    }
}