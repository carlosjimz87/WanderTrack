package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.profile.usecase.GetProfileDataUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileDataUseCase: GetProfileDataUseCase
) : ViewModel() {

    var profileState by mutableStateOf(ProfileData())
        private set

    fun loadProfile() {
        viewModelScope.launch {
            profileState = getProfileDataUseCase.execute()
        }
    }
}