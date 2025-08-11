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
            val currentUser = authRepository.currentUser

            if (currentUser?.uid.isNullOrBlank()) {
                profileState = ProfileData(username = "Guest", avatarUrl = null)
                return@launch
            }

            val uid = currentUser.uid
            val email = currentUser.email
            val avatarUrl = currentUser.photoUrl?.toString()

            val profileStats = firestoreRepository.fetchUserProfile(uid)

            // Prefer Firestore.username when available, else email, else fallback
            val baseName = when {
                !profileStats?.username.isNullOrBlank() -> profileStats!!.username!!
                !email.isNullOrBlank() -> email!!
                else -> "User${uid.takeLast(4)}"
            }

            // Apply your formatter consistently to the chosen source
            val formatted = baseName.formatUsername().capitalize(Locale.ROOT)

            profileState = ProfileData(
                username = formatted,
                avatarUrl = avatarUrl,
                countriesVisited = profileStats?.countriesVisited ?: 0,
                citiesVisited = profileStats?.citiesVisited ?: 0,
                continentsVisited = profileStats?.continentsVisited ?: 0,
                worldPercent = profileStats?.worldPercent ?: 0,
                achievements = profileStats?.achievements.orEmpty(),
            )
        }
    }
}