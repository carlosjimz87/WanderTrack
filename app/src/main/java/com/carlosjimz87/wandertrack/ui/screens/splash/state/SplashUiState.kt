package com.carlosjimz87.wandertrack.ui.screens.splash.state

data class SplashUiState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = true,           // lottie
    val showLogoAndText: Boolean = false,    // fade-in
    val preloadedCount: Int = 0,
    val errorMessage: String? = null,
    val readyToNavigate: Boolean = false
)