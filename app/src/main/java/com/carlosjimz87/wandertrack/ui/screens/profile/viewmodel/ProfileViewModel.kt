package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.domain.models.Achievement
import com.carlosjimz87.wandertrack.domain.models.ProfileUiState

class ProfileViewModel : ViewModel() {

    // Expose as val for immutability in UI
    var profileState by mutableStateOf(
        ProfileUiState(
            username = "Olivia",
            countriesVisited = 7,
            worldPercent = 4,
            citiesVisited = 32,
            continentsVisited = 2,
            achievements = listOf(
                Achievement("️✅", "First country visited", 0),
                Achievement("10", "10 countries reached", 0),
                Achievement("🚩", "All cities in a country", 0)
            )
        )
    )
        private set
}