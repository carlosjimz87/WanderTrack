package com.carlosjimz87.wandertrack.fakes

import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.domain.profile.usecase.GetProfileDataUseCase

class FakeGetProfileDataUseCase(
    private val firestoreRepository: FakeFirestoreRepositoryImpl,
    private val authRepository: FakeAuthRepositoryImpl
) : GetProfileDataUseCase(firestoreRepository, authRepository) {

    var profileDataToReturn: ProfileData? = null

    override suspend fun execute(): ProfileData {
        return profileDataToReturn ?: super.execute()
    }
}