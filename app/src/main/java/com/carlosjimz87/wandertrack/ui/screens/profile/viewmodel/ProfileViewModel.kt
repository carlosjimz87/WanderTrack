package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.formatUsername
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var profileState by mutableStateOf(ProfileData())
        private set

    fun loadProfile() {

        viewModelScope.launch {
            val rawUsername = authRepository.currentUser?.email
            val avatarUrl = authRepository.currentUser?.photoUrl?.toString()
            val formattedUsername = rawUsername?.formatUsername() ?: "User${authRepository.currentUser?.uid?.takeLast(4)}"
            val profileStats = firestoreRepository.fetchUserProfile(authRepository.currentUser?.uid ?: "")

            profileStats?.let {
                profileState = ProfileData(
                    username = formattedUsername.capitalize(Locale.ROOT),
                    avatarUrl = avatarUrl,
                    countriesVisited = it.countriesVisited,
                    citiesVisited = it.citiesVisited,
                    continentsVisited = it.continentsVisited,
                    worldPercent = it.worldPercent,
                    achievements = it.achievements,
                )
            } ?: run {
                profileState = ProfileData(
                    username = formattedUsername.capitalize(Locale.ROOT),
                    avatarUrl = avatarUrl
                )
            }

        }
    }
}