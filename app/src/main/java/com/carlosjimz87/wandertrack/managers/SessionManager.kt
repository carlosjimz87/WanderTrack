package com.carlosjimz87.wandertrack.managers

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val authRepository: AuthRepository
) {
    private val _validSession = MutableStateFlow<Boolean?>(null)
    val validSession: StateFlow<Boolean?> get() = _validSession.asStateFlow()

    init {
        _validSession.value = authRepository.isUserLoggedIn()

        FirebaseAuth.getInstance().addAuthStateListener {
            _validSession.value = authRepository.isUserLoggedIn()
        }
    }

    fun refreshSession() {
        _validSession.value = authRepository.isUserLoggedIn()
    }
}