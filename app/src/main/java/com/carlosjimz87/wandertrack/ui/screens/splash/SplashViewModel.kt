package com.carlosjimz87.wandertrack.ui.screens.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

class SplashViewModel {
    var isPlaying by mutableStateOf(true)
    var showLogoAndText by mutableStateOf(false)
    var splashStartTime by mutableLongStateOf(0L)

    suspend fun simulateLoading(minSplashTimeMs: Long) {
        val elapsed = System.currentTimeMillis() - splashStartTime
        if (elapsed < minSplashTimeMs) {
            delay(minSplashTimeMs - elapsed)
        }
    }
}