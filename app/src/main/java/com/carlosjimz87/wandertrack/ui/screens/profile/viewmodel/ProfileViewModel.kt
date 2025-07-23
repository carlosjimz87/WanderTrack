package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    var avatarUrl by mutableStateOf<String?>(null)
        private set

    init {
        loadProfile()
    }

    fun loadProfile(username: String? = null) {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            avatarUrl = authRepository.currentUser?.photoUrl?.toString()

            val profile = firestoreRepository.fetchUserProfile(userId)?.copy(
                username = username?.formatUsername() ?: "Unknown User",
            )
            if (profile != null) {
                profileState = profile
            }
        }
    }

    private fun String.formatUsername(): String {
        // use regex to extract the username from the email
        return this.substringBefore('@').replace('.', ' ').replace('_', ' ')
            .split(" ")
            .joinToString(" ")
    }
}