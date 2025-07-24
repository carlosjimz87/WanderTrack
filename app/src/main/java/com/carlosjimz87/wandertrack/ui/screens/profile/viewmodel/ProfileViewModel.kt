package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.common.formatUsername
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var profileState by mutableStateOf(ProfileData())
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            val originalProfile = firestoreRepository.fetchUserProfile(userId)
            val avatarUrl = authRepository.currentUser?.photoUrl?.toString()
            val formattedUsername = originalProfile?.username?.formatUsername()

            profileState = originalProfile?.copy(
                username = formattedUsername ?: originalProfile.username,
                avatarUrl = avatarUrl
            ) ?: ProfileData(
                username = formattedUsername ?: "Unknown User",
                avatarUrl = avatarUrl
            )
        }
    }
}