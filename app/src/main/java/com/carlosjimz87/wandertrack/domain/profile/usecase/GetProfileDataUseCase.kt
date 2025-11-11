package com.carlosjimz87.wandertrack.domain.profile.usecase

import com.carlosjimz87.wandertrack.common.formatUsername
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import java.util.Locale

open class GetProfileDataUseCase(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) {
    open suspend fun execute(): ProfileData {
        val currentUser = authRepository.currentUser

        if (currentUser?.uid.isNullOrBlank()) {
            return ProfileData(username = "Guest", avatarUrl = null)
        }

        val uid = currentUser.uid
        val email = currentUser.email
        val avatarUrl = currentUser.photoUrl?.toString()

        val profileStats = firestoreRepository.fetchUserProfile(uid)

        val baseName = when {
            !profileStats?.username.isNullOrBlank() -> profileStats.username
            !email.isNullOrBlank() -> email
            else -> "User${uid.takeLast(4)}"
        }

        val formatted = baseName.formatUsername().capitalize(Locale.ROOT)

        return ProfileData(
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