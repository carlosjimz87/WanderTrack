package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.formatUsername
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.carlosjimz87.wandertrack.utils.Logger
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

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

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

    fun clearAuthUiState() {
        Logger.d("AuthVM -> clearAuthUiState called")
        _authUiState.value = AuthUiState.Idle
    }

    init {
        sessionManager.refreshSession()
        Logger.d("AuthVM -> init: refreshSession called. isUserLoggedIn = ${authRepository.isUserLoggedIn()}")
    }

    fun loginWithEmail(email: String, password: String) {
        Logger.d("AuthVM -> loginWithEmail($email)")
        _authUiState.value = AuthUiState.Loading

        authRepository.loginWithEmail(email, password) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()
                Logger.d("AuthVM -> loginWithEmail success: uid=${_authState.value?.uid}")
                ensureUserDocument { result, error ->
                    _authUiState.value = if (result) {
                        Logger.d("AuthVM -> Firestore setup success after login")
                        AuthUiState.Success()
                    } else {
                        Logger.d("AuthVM -> Firestore setup error after login: $error")
                        AuthUiState.Error(error ?: "Firestore setup failed.")
                    }
                }
            } else {
                Logger.d("AuthVM -> loginWithEmail failed: $message")
                _authState.value = null
                sessionManager.refreshSession()
                _authUiState.value = AuthUiState.Error(message ?: "Unknown error")
            }
        }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        Logger.d("AuthVM -> loginWithGoogle started")
        authRepository.loginWithGoogle(idToken) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()
                Logger.d("AuthVM -> loginWithGoogle success: uid=${_authState.value?.uid} " +
                        "emailveified=${_authState.value?.isEmailVerified} " +
                        "Provider: ${_authState.value?.providerData?.joinToString { it.providerId }}")

                ensureUserDocument(onResult)
            } else {
                Logger.d("AuthVM -> loginWithGoogle failed: $message")
                onResult(false, message)
            }
        }
    }

    fun signup(email: String, password: String) {
        Logger.d("AuthVM -> signup($email)")
        _authUiState.value = AuthUiState.Loading
        authRepository.signup(email, password) { success, message ->
            if (success) {
                Logger.d("AuthVM -> signup success: ${message ?: "no message"}")
                _authUiState.value =
                    AuthUiState.Success(message ?: "Signup successful. Please verify your email.")
            } else {
                Logger.d("AuthVM -> signup failed: $message")
                _authUiState.value = AuthUiState.Error(message ?: "Unknown error during signup")
            }
        }
    }

    fun logout() {
        Logger.d("AuthVM -> logout called")
        authRepository.logout()
        _authState.value = null
        sessionManager.refreshSession()
        _authUiState.value = AuthUiState.Idle
        Logger.d("AuthVM -> logout completed")
    }

    private fun ensureUserDocument(onResult: (Boolean, String?) -> Unit) {
        val userId = authRepository.currentUser?.uid ?: run {
            Logger.d("AuthVM -> ensureUserDocument failed: user is null")
            onResult(false, "User not found")
            return
        }

        Logger.d("AuthVM -> ensuring Firestore user document for uid=$userId")
        viewModelScope.launch {
            try {
                firestoreRepository.ensureUserDocument(userId)
                firestoreRepository.recalculateAndUpdateStats(userId)
                Logger.d("AuthVM -> Firestore user document ensured and stats recalculated")
                onResult(true, null)
            } catch (e: Exception) {
                Logger.d("AuthVM -> Firestore setup exception: ${e.message}")
                e.printStackTrace()
                onResult(false, "Failed to setup Firestore")
            }
        }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = authRepository.currentUser
        if (user == null) {
            Logger.d("AuthVM -> deleteAccount failed: user is null")
            onResult(false, "User not logged in")
            return
        }

        Logger.d("AuthVM -> deleteAccount started for uid=${user.uid}")
        viewModelScope.launch {
            try {
                firestoreRepository.deleteUserDocument(user.uid)
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.d("AuthVM -> Firebase user deleted successfully")
                        _authState.value = null
                        sessionManager.refreshSession()
                        onResult(true, null)
                    } else {
                        Logger.d("AuthVM -> Firebase user deletion failed: ${task.exception?.message}")
                        onResult(false, task.exception?.message)
                    }
                }
            } catch (e: Exception) {
                Logger.d("AuthVM -> Exception deleting account: ${e.message}")
                onResult(false, "Error deleting account: ${e.message}")
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        Logger.d("AuthVM -> resendVerificationEmail called")
        authRepository.resendVerificationEmail(onResult)
    }
}