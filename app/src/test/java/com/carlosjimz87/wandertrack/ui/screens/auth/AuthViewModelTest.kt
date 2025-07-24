package com.carlosjimz87.wandertrack.ui.screens.auth

import com.carlosjimz87.wandertrack.data.repo.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.fakes.FakeSessionManagerImpl
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

    private lateinit var authRepo: FakeAuthRepositoryImpl
    private lateinit var firestoreRepo: FirestoreRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepo = FakeAuthRepositoryImpl()
        firestoreRepo = FakeFirestoreRepositoryImpl()
        sessionManager = FakeSessionManagerImpl(authRepo)

        viewModel = AuthViewModel(
            authRepo,
            firestoreRepo,
            sessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signup success updates authState`() = runTest {
        viewModel.signup("test@test.com", "password") { success, _ ->
            assertTrue(success)
        }
        assertNotNull(viewModel.authState.first())
    }

    @Test
    fun `validSession is true after login`() = runTest {
        authRepo.isEmailVerified = true
        viewModel.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        assertTrue(viewModel.validSession.value == true)
    }

    @Test
    fun `loginWithEmail success updates authUiState and authState`() = runTest {
        authRepo.isEmailVerified = true

        viewModel.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val uiState = viewModel.authUiState.value
        val user = viewModel.authState.value

        assertTrue(uiState is AuthUiState.Success)
        assertNotNull(user)
    }

    @Test
    fun `loginWithEmail fails if user is not verified`() = runTest {
        authRepo.isEmailVerified = false

        viewModel.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val uiState = viewModel.authUiState.value
        val user = viewModel.authState.value

        assertTrue(uiState is AuthUiState.Error)
        assertEquals("Please verify your email before continuing.", (uiState as AuthUiState.Error).message)
        assertNull(user)
    }

    @Test
    fun `loginWithEmail fails if repo returns failure`() = runTest {
        authRepo.shouldFail = true

        viewModel.loginWithEmail("fail@test.com", "password")
        advanceUntilIdle()

        val uiState = viewModel.authUiState.value
        val user = viewModel.authState.value

        assertTrue(uiState is AuthUiState.Error)
        assertEquals("Login failed", (uiState as AuthUiState.Error).message)
        assertNull(user)
    }

    @Test
    fun `loginWithEmail emits Loading before Success`() = runTest {
        authRepo.isEmailVerified = true

        val collectedStates = mutableListOf<AuthUiState>()
        val job = launch {
            viewModel.authUiState.toList(collectedStates)
        }

        viewModel.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()
        job.cancel()

        assertTrue(collectedStates.first() is AuthUiState.Loading)
        assertTrue(collectedStates.last() is AuthUiState.Success)
    }

    @Test
    fun `logout clears authState`() = runTest {
        authRepo.isEmailVerified = true
        viewModel.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        assertNotNull(viewModel.authState.value)

        // Logout y verificar que authState se borra
        viewModel.logout()
        assertNull(viewModel.authState.value)
        assertTrue(viewModel.authUiState.value is AuthUiState.Idle)
    }

    @Test
    fun `loginWithGoogle success updates authState`() = runTest {
        authRepo.googleLoginSuccess = true

        viewModel.loginWithGoogle("token") { success, _ ->
            assertTrue(success)
        }

        advanceUntilIdle()
        assertNotNull(viewModel.authState.value)
    }

    @Test
    fun `loginWithGoogle failure does not update authState`() = runTest {
        authRepo.googleLoginSuccess = false

        viewModel.loginWithGoogle("token") { success, _ ->
            assertFalse(success)
        }

        advanceUntilIdle()
        assertNull(viewModel.authState.value)
    }

    @Test
    fun `resendVerificationEmail succeeds when user exists`() = runTest {
        authRepo.loginWithEmail("test@test.com", "password") { _, _ -> }
        authRepo.isEmailVerified = false

        var result: Pair<Boolean, String?>? = null

        viewModel.resendVerificationEmail { success, msg ->
            result = success to msg
        }

        assertEquals(true to "Verification email sent.", result)
    }

}