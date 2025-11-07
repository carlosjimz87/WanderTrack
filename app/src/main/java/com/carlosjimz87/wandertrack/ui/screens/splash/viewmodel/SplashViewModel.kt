package com.carlosjimz87.wandertrack.ui.screens.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.usecase.PreloadCountriesUseCase
import com.carlosjimz87.wandertrack.managers.LocalDataManager
import com.carlosjimz87.wandertrack.ui.screens.splash.state.SplashUiState
import com.carlosjimz87.wandertrack.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class SplashViewModel(
    private val preloadCountriesUseCase: PreloadCountriesUseCase,
    private val localDataManager: LocalDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    fun start(minSplashTimeMs: Long = 1000L) {
        // Evita relanzar si ya está en curso o listo
        if (!_uiState.value.isLoading) return

        viewModelScope.launch {
            val t0 = System.nanoTime()

            // Arranque: reproducimos lottie
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isPlaying = true,
                    showLogoAndText = false,
                    errorMessage = null
                )
            }

            // IO pesado: leer y parsear JSON
            val countries: List<Country> = try {
                preloadCountriesUseCase().also {
                    Logger.d("SplashVM → ✅ Preloaded ${it.size} countries")
                }
            } catch (e: Exception) {
                Logger.e("SplashVM → ❌ Error: ${e.message}")
                emptyList()
            }

            // Persistimos en un hilo de fondo
            withContext(Dispatchers.Default) {
                localDataManager.preloadedCountries = countries
            }

            // Espera el mínimo de splash (sin bloquear animaciones)
            val elapsedMs = (System.nanoTime() - t0) / 1_000_000
            val remaining = max(0L, minSplashTimeMs - elapsedMs)
            if (remaining > 0) kotlinx.coroutines.delay(remaining)

            // Fade-in de logo/texto
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showLogoAndText = true,
                    isPlaying = false,
                    preloadedCount = countries.size,
                    readyToNavigate = countries.isNotEmpty(),
                    errorMessage = if (countries.isEmpty()) "No se pudieron cargar los países" else null
                )
            }
        }
    }
}