
package com.carlosjimz87.wandertrack.ui.screens.auth.state

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val isSignupSuccessful: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val verificationEmailSent: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showResendButton: Boolean = false,
    val blockNavigation: Boolean = false,
)
