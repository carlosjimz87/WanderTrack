package com.carlosjimz87.wandertrack.ui.screens.auth.state

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val showResendButton: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val blockNavigation: Boolean = false
)