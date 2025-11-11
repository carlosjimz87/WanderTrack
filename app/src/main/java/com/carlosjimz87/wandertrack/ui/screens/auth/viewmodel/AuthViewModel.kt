package com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.auth.usecase.DeleteAccountUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.EnsureUserDocumentUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LoginWithGoogleUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.LogoutUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.ResendVerificationEmailUseCase
import com.carlosjimz87.wandertrack.domain.auth.usecase.SignupUseCase
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val signupUseCase: SignupUseCase,
    private val resendVerificationEmailUseCase: ResendVerificationEmailUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val ensureUserDocumentUseCase: EnsureUserDocumentUseCase,
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
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
            _authUiState.value = AuthUiState(isLoading = true)

            val result = loginWithEmailUseCase.execute(email, password)
            if (result.isSuccess) {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()
                _authUiState.value = AuthUiState(isLoginSuccessful = true)
            } else {
                val code = result.exceptionOrNull()?.message ?: "LOGIN_FAILED"
                val isNotVerified = code == "EMAIL_NOT_VERIFIED"

                _authState.value = null
                sessionManager.endSession()

                _authUiState.value = AuthUiState(
                    errorMessage = code,
                    showResendButton = isNotVerified,
                    blockNavigation = isNotVerified
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)

            runCatching {
                loginWithGoogleUseCase.execute(idToken)
                ensureUserDocumentUseCase.execute()
            }.onSuccess {
                _authState.value = authRepository.currentUser
                sessionManager.refreshSession()
                _authUiState.value = AuthUiState(isLoginSuccessful = true)
            }.onFailure {
                _authState.value = null
                sessionManager.endSession()
                _authUiState.value = AuthUiState(
                    errorMessage = it.message ?: "LOGIN_WITH_GOOGLE_FAILED",
                    blockNavigation = false
                )
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)

            val result = signupUseCase.execute(email, password)
            _authUiState.value = if (result.isSuccess) {
                AuthUiState(
                    isSignupSuccessful = true,
                    verificationEmailSent = true,
                )
            } else {
                AuthUiState(
                    errorMessage = (result.exceptionOrNull()?.message ?: "SIGNUP_FAILED"),
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = resendVerificationEmailUseCase.execute()
            _authUiState.value = if (result.isSuccess) {
                AuthUiState(
                    verificationEmailSent = true,
                    errorMessage = "VERIFICATION_EMAIL_SENT" // stable code for tests/i18n
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
        logoutUseCase.execute()
        _authState.value = null
        sessionManager.endSession()   // make it explicit false
        _authUiState.value = AuthUiState()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)

            runCatching {
                deleteAccountUseCase.execute()
            }.onSuccess {
                _authState.value = null
                sessionManager.endSession()
                _authUiState.value = AuthUiState(
                    isAccountDeleted = true,
                    successMessage = "ACCOUNT_DELETED"
                )
            }.onFailure {
                _authUiState.value = AuthUiState(
                    errorMessage = it.message ?: "ACCOUNT_DELETE_FAILED",
                    isLoading = false,
                    isAccountDeleted = false
                )
            }
        }
    }
}