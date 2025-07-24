package com.carlosjimz87.wandertrack.ui.screens.auth.state

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String? = null) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}