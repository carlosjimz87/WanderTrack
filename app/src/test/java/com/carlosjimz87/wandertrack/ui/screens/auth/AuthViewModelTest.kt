package com.carlosjimz87.wandertrack.ui.screens.auth

import com.carlosjimz87.wandertrack.data.repo.fakes.FakeAuthRepository
import com.carlosjimz87.wandertrack.data.repo.fakes.FakeFirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthScreenState
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepo: FakeAuthRepository
    private lateinit var firestoreRepo: FakeFirestoreRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepo = FakeAuthRepository()
        firestoreRepo = FakeFirestoreRepository()
        viewModel = AuthViewModel(authRepo, firestoreRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates authState`() = runTest {
        viewModel.loginWithEmail("test@test.com", "password") { success, _ ->
            assertTrue(success)
        }
        assertNotNull(viewModel.authState.first())
    }

    @Test
    fun `login failure does not update authState`() = runTest {
        authRepo.shouldFail = true

        viewModel.loginWithEmail("test@test.com", "password") { success, message ->
            assertFalse(success)
            assertEquals("Login failed", message)
        }

        assertNull(viewModel.authState.first())
    }

    @Test
    fun `signup success updates authState`() = runTest {
        viewModel.signup("test@test.com", "password") { success, _ ->
            assertTrue(success)
        }
        assertNotNull(viewModel.authState.first())
    }

    @Test
    fun `logout clears authState`() = runTest {
        viewModel.loginWithEmail("test@test.com", "password") { _, _ -> }
        assertNotNull(viewModel.authState.first())

        viewModel.logout()
        assertNull(viewModel.authState.first())
    }

    @Test
    fun `showLogin updates authScreenState`() = runTest {
        viewModel.showLogin()
        assertEquals(AuthScreenState.LOGIN, viewModel.authScreenState.first())
    }

    @Test
    fun `showSignup updates authScreenState`() = runTest {
        viewModel.showSignup()
        assertEquals(AuthScreenState.SIGNUP, viewModel.authScreenState.first())
    }

    @Test
    fun `showStart updates authScreenState`() = runTest {
        viewModel.showStart()
        assertEquals(AuthScreenState.START, viewModel.authScreenState.first())
    }

    @Test
    fun `loginWithGoogle updates authState on success`() = runTest {
        authRepo.setGoogleLoginResult(success = true)

        var callbackSuccess = false

        viewModel.loginWithGoogle("fakeToken") { success, _ ->
            callbackSuccess = success
        }

        advanceUntilIdle() // Ensures coroutine updates are completed

        assertTrue(callbackSuccess)
        assertNotNull(viewModel.authState.value)
    }

    @Test
    fun `loginWithGoogle does not update authState on failure`() = runTest {
        authRepo.setGoogleLoginResult(success = false)

        var callbackSuccess = true

        viewModel.loginWithGoogle("fakeToken") { success, _ ->
            callbackSuccess = success
        }

        assertFalse(callbackSuccess)
        assertNull(viewModel.authState.value)
    }
}