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
import kotlinx.coroutines.yield

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authState = MutableStateFlow(authRepository.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    val validSession: StateFlow<Boolean?> get() = sessionManager.validSession

    init {
        sessionManager.refreshSession()
    }

    fun clearUiState() {
        _authUiState.value = AuthUiState()
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            yield()

            val result = authRepository.loginWithEmail(email, password)
            if (result.isSuccess) {
                // success
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true,
                    showResendButton = false,
                    blockNavigation = false
                )
            } else {
                // failure
                val code = result.exceptionOrNull()?.message ?: "LOGIN_FAILED"
                val isNotVerified = code == "EMAIL_NOT_VERIFIED"

                _authState.value = null
                sessionManager.endSession()

                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = false,
                    errorMessage = code,              // keep codes in state
                    showResendButton = isNotVerified, // show resend only for not-verified
                    blockNavigation = isNotVerified
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authUiState.value =
                AuthUiState(isLoading = true, errorMessage = null, successMessage = null)

            yield()

            authRepository.loginWithGoogle(idToken)
                .onSuccess {
                    _authState.value = authRepository.currentUser
                    sessionManager.refreshSession()

                    ensureUserDocument { success, error ->
                        _authUiState.value = if (success) {
                            AuthUiState(isLoginSuccessful = true)
                        } else {
                            AuthUiState(errorMessage = error, blockNavigation = true)
                        }
                    }
                }
                .onFailure {
                    _authState.value = null
                    sessionManager.endSession()
                    _authUiState.value = AuthUiState(
                        errorMessage = it.message ?: "LOGIN_WITH_GOOGLE_FAILED",
                        blockNavigation = true
                    )
                }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(
                isLoading = true, errorMessage = null, successMessage = null
            )

            yield()

            val result = authRepository.signup(email, password)
            _authUiState.value = if (result.isSuccess) {
                _authUiState.value.copy(
                    isLoading = false,
                    successMessage = "SIGNUP_OK_NEEDS_VERIFICATION", // stable code
                    verificationEmailSent = true,
                    blockNavigation = true,
                    isLoginSuccessful = false
                )
            } else {
                _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = (result.exceptionOrNull()?.message ?: "SIGNUP_FAILED"),
                    verificationEmailSent = false
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = authRepository.resendVerificationEmail()
            _authUiState.value = if (result.isSuccess) {
                AuthUiState(
                    verificationEmailSent = true,
                    successMessage = "VERIFICATION_EMAIL_SENT" // stable code for tests/i18n
                )
            } else {
                AuthUiState(
                    verificationEmailSent = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "VERIFICATION_EMAIL_FAILED"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
        sessionManager.endSession()   // make it explicit false
        _authUiState.value = AuthUiState()
    }

    fun deleteAccount() {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true)

            yield()

            try {
                firestoreRepository.deleteUserDocument(user.uid)
                user.delete().addOnCompleteListener { task ->
                    _authUiState.value = if (task.isSuccessful) {
                        _authState.value = null
                        sessionManager.endSession()
                        AuthUiState(
                            isAccountDeleted = true,
                            successMessage = "ACCOUNT_DELETED"
                        )
                    } else {
                        AuthUiState(
                            errorMessage = task.exception?.message ?: "ACCOUNT_DELETE_FAILED",
                            isLoading = false,
                            isAccountDeleted = false
                        )
                    }
                }
            } catch (e: Exception) {
                _authUiState.value = AuthUiState(
                    errorMessage = "Error deleting account: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun ensureUserDocument(onResult: (Boolean, String?) -> Unit) {
        val userId = authRepository.currentUser?.uid ?: run {
            onResult(false, "USER_NOT_FOUND")
            return
        }
        viewModelScope.launch {
            runCatching {
                firestoreRepository.ensureUserDocument(userId)
                firestoreRepository.recalculateAndUpdateStats(userId)
            }.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.localizedMessage) }
        }
    }
}