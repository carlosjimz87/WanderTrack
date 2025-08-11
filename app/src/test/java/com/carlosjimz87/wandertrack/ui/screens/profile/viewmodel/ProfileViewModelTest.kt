package com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel

import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var authRepo: FakeAuthRepositoryImpl
    private lateinit var firestoreRepo: FakeFirestoreRepositoryImpl
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testUserId = "testUserId"

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)

        authRepo = FakeAuthRepositoryImpl().apply {
            isEmailVerified = true                 // must be true for success
            nextEmailLogin =
                FakeAuthRepositoryImpl.Outcome.Success       // your fake’s control knob
        }
        // perform the actual "login" to populate _fakeUser and notify listeners
        val res = authRepo.loginWithEmail("test.user@example.com", "password")
        assertTrue(res.isSuccess)

        firestoreRepo = FakeFirestoreRepositoryImpl().apply {
            setFakeProfile(testUserId, ProfileData(username = "Test User"))
        }

        viewModel = ProfileViewModel(firestoreRepo, authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile loads user data and avatar`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Test User", viewModel.profileState.username)
        assertEquals("https://fake.com/avatar.png", viewModel.profileState.avatarUrl)
    }

    @Test
    fun `loadProfile formats username from Firestore`() = runTest {
        val rawUsername = "carlos.jimz_dev@domain.com"

        firestoreRepo.setFakeProfile(
            userId = testUserId,
            profile = ProfileData(username = rawUsername)
        )
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("Carlos.jimz_...", viewModel.profileState.username)
    }

    @Test
    fun `loadProfile does nothing if user is null`() = runTest {
        authRepo.logout() // esto borra el usuario
        viewModel = ProfileViewModel(firestoreRepo, authRepo)

        advanceUntilIdle()

        assertEquals(ProfileData(), viewModel.profileState)
        assertNull(viewModel.profileState.avatarUrl)
    }
}