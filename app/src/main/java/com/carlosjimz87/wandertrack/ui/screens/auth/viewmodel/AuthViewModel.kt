package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.formatUsername
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.managers.SessionManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow(authRepository.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    val validSession: StateFlow<Boolean?>
        get() = sessionManager.validSession

    val userId: String?
        get() = _authState.value?.uid

    val userEmail: String?
        get() = _authState.value?.email

    val userName: String?
        get() = userEmail?.formatUsername()

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        authRepository.loginWithEmail(email, password) { success, message ->
            val user = authRepository.currentUser
            if (success && user?.isEmailVerified == true) {
                _authState.value = user
                sessionManager.refreshSession()
                ensureUserDocument(onResult)
                onResult(true, null)
            } else if (user != null && !user.isEmailVerified) {
                onResult(false, "Please verify your email before logging in.")
            } else {
                onResult(false, message)
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
                sessionManager.refreshSession()
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
                _authState.value = authRepository.currentUser
                authRepository.resendVerificationEmail { _, _ -> }
                onResult(true, "Account created. Please verify your email before logging in.")
            } else {
                onResult(false, message)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
        sessionManager.refreshSession()
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

    fun deleteAccount(
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = authRepository.currentUser
        if (user == null) {
            onResult(false, "User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                firestoreRepository.deleteUserDocument(user.uid)

                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = null
                        sessionManager.refreshSession()
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error deleting account: ${e.message}")
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        authRepository.resendVerificationEmail(onResult)
    }
}