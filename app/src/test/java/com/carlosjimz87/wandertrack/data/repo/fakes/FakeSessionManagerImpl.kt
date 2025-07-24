package com.carlosjimz87.wandertrack.data.repo.fakes

import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionManagerImpl(
    private val authRepository: AuthRepository
) : SessionManager {

    private val _fakeValidSession = MutableStateFlow<Boolean?>(null)
    override val validSession: StateFlow<Boolean?>
        get() = _fakeValidSession.asStateFlow()

    override fun refreshSession() {
        _fakeValidSession.value = authRepository.isUserLoggedIn()
    }
}