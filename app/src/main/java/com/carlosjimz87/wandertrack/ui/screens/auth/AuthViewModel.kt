package com.carlosjimz87.wandertrack.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.data.repo.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(authRepository.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    private val _authScreenState = MutableStateFlow(AuthScreenState.START)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    fun showLogin() { _authScreenState.value = AuthScreenState.LOGIN }
    fun showSignup() { _authScreenState.value = AuthScreenState.SIGNUP }
    fun showStart() { _authScreenState.value = AuthScreenState.START }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        authRepository.login(email, password) { success, message ->
            if (success) _authState.value = authRepository.currentUser
            onResult(success, message)
        }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        authRepository.loginWithGoogle(idToken) { success, message ->
            if (success) _authState.value = authRepository.currentUser
            onResult(success, message)
        }
    }

    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        authRepository.signup(email, password) { success, message ->
            if (success) _authState.value = authRepository.currentUser
            onResult(success, message)
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
    }
}