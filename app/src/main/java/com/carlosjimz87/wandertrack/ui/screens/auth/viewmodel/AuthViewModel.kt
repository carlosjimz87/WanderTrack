package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.data.repo.AuthRepository
import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthScreenState
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(authRepository.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    private val _authScreenState = MutableStateFlow(AuthScreenState.START)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    fun showLogin() { _authScreenState.value = AuthScreenState.LOGIN }
    fun showSignup() { _authScreenState.value = AuthScreenState.SIGNUP }
    fun showStart() { _authScreenState.value = AuthScreenState.START }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        authRepository.loginWithEmail(email, password) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                ensureUserDocument(onResult)
                onResult(true, null)
            } else {
                onResult(false, message)
                Logger.e("Error in email login [$message]")
            }
        }
    }

    fun loginWithGoogle(
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        authRepository.loginWithGoogle(idToken) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                ensureUserDocument(onResult)
            } else {
                onResult(false, message)
            }
        }
    }


    fun signup(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        authRepository.signup(email, password) { success, message ->
            if (success) {
                onResult(true, "Account created. Please verify your email before logging in.")
            } else {
                onResult(false, message)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
    }

    private fun ensureUserDocument(onResult: (Boolean, String?) -> Unit) {
        val userId = authRepository.currentUser?.uid ?: run {
            onResult(false, "User not found")
            return
        }

        viewModelScope.launch {
            try {
                firestoreRepository.ensureUserDocument(userId)
                firestoreRepository.recalculateAndUpdateStats(userId)
                onResult(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Failed to setup Firestore")
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        authRepository.resendVerificationEmail(onResult)
    }
}