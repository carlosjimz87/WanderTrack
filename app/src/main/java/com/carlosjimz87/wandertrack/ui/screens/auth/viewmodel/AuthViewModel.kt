package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
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

    init {
        sessionManager.refreshSession()
    }

    fun clearUiState() {
        _uiState.value = AuthUiState()
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            authRepository.loginWithEmail(email, password).fold(
                onSuccess = {
                    _authState.value = authRepository.currentUser
                    sessionManager.refreshSession()

                    ensureUserDocument { success, error ->
                        _uiState.value = if (success) {
                            AuthUiState(isLoginSuccessful = true)
                        } else {
                            AuthUiState(errorMessage = error, blockNavigation = true)
                        }
                    }
                },
                onFailure = {
                    val showResend = it.message?.contains("verify", ignoreCase = true) == true
                    _uiState.value = AuthUiState(
                        errorMessage = it.message,
                        showResendButton = showResend,
                        blockNavigation = true
                    )
                }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.loginWithGoogle(idToken).fold(
                onSuccess = {
                    _authState.value = authRepository.currentUser
                    sessionManager.refreshSession()

                    ensureUserDocument { success, error ->
                        _uiState.value = if (success) {
                            AuthUiState(isLoginSuccessful = true)
                        } else {
                            AuthUiState(errorMessage = error, blockNavigation = true)
                        }
                    }
                },
                onFailure = {
                    _uiState.value = AuthUiState(
                        errorMessage = it.message,
                        blockNavigation = true
                    )
                }
            )
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.signup(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(successMessage = "We've sent you a verification email.")
            } else {
                AuthUiState(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = authRepository.resendVerificationEmail()
            _uiState.value = if (result.isSuccess) {
                AuthUiState(verificationEmailSent = true, successMessage = "Verification email sent.")
            } else {
                AuthUiState(verificationEmailSent = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
        sessionManager.refreshSession()
        _uiState.value = AuthUiState()
    }

    fun deleteAccount() {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                firestoreRepository.deleteUserDocument(user.uid)
                user.delete().addOnCompleteListener { task ->
                    _uiState.value = if (task.isSuccessful) {
                        _authState.value = null
                        sessionManager.refreshSession()
                        AuthUiState(isAccountDeleted = true, successMessage = "Account deleted successfully")
                    } else {
                        AuthUiState(
                            errorMessage = task.exception?.message,
                            isLoading = false,
                            isAccountDeleted = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = "Error deleting account: ${e.message}",
                    isLoading = false
                )
            }
        }
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
}