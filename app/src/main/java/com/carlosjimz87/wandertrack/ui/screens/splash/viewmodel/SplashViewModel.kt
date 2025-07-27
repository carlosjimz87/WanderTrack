package com.carlosjimz87.wandertrack.ui.screens.splash.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.managers.LocalDataManager
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class SplashViewModel(
    private val localDataManager: LocalDataManager
) : ViewModel() {

    var splashStartTime: Long = 0L
    var showLogoAndText by mutableStateOf(false)
    var isPlaying by mutableStateOf(true)

    /**
     * Loads all countries and cities from the raw JSON and notifies via callback.
     */
    fun loadData(context: Context, onDataLoaded: (List<Country>) -> Unit) {
        viewModelScope.launch {
            val countries = preloadCountriesWithCities(context)

            if (countries.isNotEmpty()) {
                Logger.d("SplashVM → ✅ Preloaded ${countries.size} countries")
            } else {
                Logger.e("SplashVM → ❌ Failed to load countries")
            }

            onDataLoaded(countries)
        }
    }

    /**
     * Parses the JSON file from raw resources and stores it in LocalDataManager.
     */
    private fun preloadCountriesWithCities(context: Context): List<Country> {
        return runCatching {
            context.resources.openRawResource(R.raw.country_codes)
                .bufferedReader()
                .use { it.readText() }
        }.mapCatching { json ->
            val type = object : TypeToken<List<Country>>() {}.type
            Gson().fromJson<List<Country>>(json, type)
        }.onSuccess { countries ->
            localDataManager.preloadedCountries = countries
        }.onFailure { error ->
            Logger.e("SplashVM → ❌ Error loading countries: ${error.message}")
        }.getOrElse {
            emptyList()
        }
    }
}