package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeGetProfileDataUseCase
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var authRepo: FakeAuthRepositoryImpl
    private lateinit var firestoreRepo: FakeFirestoreRepositoryImpl
    private lateinit var getProfileDataUseCase: FakeGetProfileDataUseCase
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepo = FakeAuthRepositoryImpl()
        firestoreRepo = FakeFirestoreRepositoryImpl()
        getProfileDataUseCase = FakeGetProfileDataUseCase(firestoreRepo, authRepo)
        viewModel = ProfileViewModel(getProfileDataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile loads user data and avatar`() = runTest {
        authRepo.seedLoggedUser()
        firestoreRepo.setFakeProfile(
            "uid", ProfileData(
                username = "Test User",
                avatarUrl = "https://fake.com/avatar.png"
            )
        )

        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Test User", viewModel.profileState.username)
        assertEquals("https://fake.com/avatar.png", viewModel.profileState.avatarUrl)
    }

    @Test
    fun `loadProfile formats username from Firestore`() = runTest {
        authRepo.seedLoggedUser()
        val rawUsername = "carlos.jimz_dev@domain.com"
        firestoreRepo.setFakeProfile(
            "uid",
            ProfileData(username = rawUsername)
        )
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Carlos.jimz_...", viewModel.profileState.username)
    }

    @Test
    fun `loadProfile does nothing if user is null`() = runTest {
        authRepo.logout()
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Guest", viewModel.profileState.username)
        assertNull(viewModel.profileState.avatarUrl)
    }
}