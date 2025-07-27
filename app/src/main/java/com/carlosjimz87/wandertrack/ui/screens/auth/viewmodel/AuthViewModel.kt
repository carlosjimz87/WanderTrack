package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow(authRepository.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    val validSession: StateFlow<Boolean?>
        get() = sessionManager.validSession

    fun clearUiState() {
        _uiState.value = AuthUiState()
    }

    init {
        sessionManager.refreshSession()
    }

    fun loginWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        authRepository.loginWithEmail(email, password) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()

                ensureUserDocument { result, error ->
                    _uiState.value = if (result) {
                        AuthUiState(isLoginSuccessful = true, blockNavigation = false, isLoading = false)
                    } else {
                        AuthUiState(errorMessage = error, blockNavigation = true, isLoading = false)
                    }
                }
            } else {
                val showResend = message?.contains("verify", ignoreCase = true) == true
                _uiState.value = AuthUiState(
                    errorMessage = message,
                    showResendButton = showResend,
                    blockNavigation = true,
                    isLoading = false
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        authRepository.loginWithGoogle(idToken) { success, message ->
            if (success) {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()

                Logger.d(
                    "AuthVM -> loginWithGoogle success: uid=${_authState.value?.uid} " +
                            "emailVerified=${_authState.value?.isEmailVerified} " +
                            "Provider: ${_authState.value?.providerData?.joinToString { it.providerId }}"
                )

                ensureUserDocument { result, ensureMsg ->
                    _uiState.value = if (result) {
                        AuthUiState(isLoginSuccessful = true, blockNavigation = false)
                    } else {
                        AuthUiState(errorMessage = ensureMsg, blockNavigation = true)
                    }
                }

            } else {
                _uiState.value = AuthUiState(
                    errorMessage = message ?: "Google login unknown error",
                    blockNavigation = true
                )
            }
        }
    }

    fun signup(email: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true)
        authRepository.signup(email, password) { success, message ->
            _uiState.value = if (success) {
                AuthUiState(successMessage = message)
            } else {
                AuthUiState(errorMessage = message)
            }
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        authRepository.resendVerificationEmail(onResult)
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
        sessionManager.refreshSession()
        _uiState.value = AuthUiState()
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
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun deleteAccount() {
        val user = authRepository.currentUser
        if (user == null) {
            Logger.d("AuthVM -> deleteAccount failed: user is null")
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in",
                isLoading = false,
                isAccountDeleted = false
            )
            return
        }

        Logger.d("AuthVM -> deleteAccount started for uid=${user.uid}")
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                firestoreRepository.deleteUserDocument(user.uid)

                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Logger.d("AuthVM -> Firebase user deleted successfully")
                        _authState.value = null
                        sessionManager.refreshSession()
                        _uiState.value = AuthUiState(
                            isAccountDeleted = true,
                            successMessage = "Account deleted successfully"
                        )
                    } else {
                        val error = task.exception?.message ?: "Unknown error"
                        Logger.d("AuthVM -> Firebase user deletion failed: $error")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAccountDeleted = false,
                            errorMessage = error
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.d("AuthVM -> Exception deleting account: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAccountDeleted = false,
                    errorMessage = "Error deleting account: ${e.message}"
                )
            }
        }
    }
}