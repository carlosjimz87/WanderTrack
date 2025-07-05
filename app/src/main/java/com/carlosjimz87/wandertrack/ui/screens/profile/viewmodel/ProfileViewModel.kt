package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.domain.models.Achievement
import com.carlosjimz87.wandertrack.domain.models.ProfileData

class ProfileViewModel : ViewModel() {

    var profileState = mutableStateOf(
        ProfileData(
            username = "Olivia",
            countriesVisited = 7,
            worldPercent = 4,
            citiesVisited = 32,
            continentsVisited = 2,
            achievements = listOf(
                Achievement("✔️", "First country visited", 0),
                Achievement("10", "10 countries reached", 0),
                Achievement("🏳️", "All cities in a country", 0)
            )
        )
    )
}