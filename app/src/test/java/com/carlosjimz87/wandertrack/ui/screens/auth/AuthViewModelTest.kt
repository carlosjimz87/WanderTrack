package com.carlosjimz87.wandertrack.ui.screens.auth

import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeSessionManagerImpl
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
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
    private lateinit var vm: AuthViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepo = FakeAuthRepositoryImpl()
        firestoreRepo = FakeFirestoreRepositoryImpl()
        sessionManager = FakeSessionManagerImpl(authRepo)
        vm = AuthViewModel(authRepo, firestoreRepo, sessionManager)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // --------- Helpers ----------
    private fun AuthUiState.assertIdle(
        loading: Boolean = false,
        error: String? = null,
        success: String? = null
    ) {
        assertEquals(loading, isLoading)
        assertEquals(error, errorMessage)
        assertEquals(success, successMessage)
    }

    // --------- Tests ---------

    @Test
    fun `signup success sets verification flags and successMessage`() = runTest(dispatcher) {
        authRepo.nextSignup = FakeAuthRepositoryImpl.Outcome.Success

        vm.signup("test@test.com", "password")
        advanceUntilIdle()

        val s = vm.authUiState.value
        s.assertIdle(loading = false, success = "SIGNUP_OK_NEEDS_VERIFICATION")
        assertTrue(s.verificationEmailSent)
        assertFalse(s.isLoginSuccessful)
        assertTrue(s.blockNavigation) // si bloqueas hasta verificar
    }

    @Test
    fun `email login success updates flags and session`() = runTest(dispatcher) {
        authRepo.isEmailVerified = true
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertFalse(s.isLoading)
        assertTrue(s.isLoginSuccessful)
        assertNull(s.errorMessage)
        assertNotNull(vm.authState.value)
        assertTrue(vm.validSession.value == true)
    }

    @Test
    fun `email login blocked when email not verified`() = runTest(dispatcher) {
        authRepo.isEmailVerified = false
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertFalse(s.isLoading)
        assertFalse(s.isLoginSuccessful)
        assertEquals("EMAIL_NOT_VERIFIED", s.errorMessage)   // tu mapper luego lo traduce
        assertTrue(s.showResendButton)
        assertTrue(s.blockNavigation)
        assertNull(vm.authState.value)
    }

    @Test
    fun `email login maps common errors (table driven)`() = runTest(dispatcher) {
        val cases = listOf("WRONG_PASSWORD", "USER_NOT_FOUND", "TOO_MANY_REQUESTS")
        for (code in cases) {
            authRepo.reset()
            authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Error(code)

            vm.loginWithEmail("fail@test.com", "x")
            advanceUntilIdle()

            val s = vm.authUiState.value
            assertFalse(s.isLoading)
            assertFalse(s.isLoginSuccessful)
            assertEquals(code, s.errorMessage)
            assertFalse(s.showResendButton)
            assertNull(vm.authState.value)
        }
    }

    @Test
    fun `authUiState emits Loading then success on email login`() = runTest(dispatcher) {
        authRepo.isEmailVerified = true
        authRepo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        val emissions = mutableListOf<AuthUiState>()
        val job = launch {
            vm.authUiState
                .drop(1)   // skip initial idle
                .take(2)   // Loading, then Success
                .toList(emissions)
        }

        // ✅ ensure collector is active
        advanceUntilIdle()

        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()
        job.cancel()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0].isLoading)
        val last = emissions[1]
        assertFalse(last.isLoading)
        assertTrue(last.isLoginSuccessful)
        assertNull(last.errorMessage)
    }

    @Test
    fun `logout clears authState and resets flags`() = runTest(dispatcher) {
        authRepo.isEmailVerified = true
        vm.loginWithEmail("test@test.com", "password")
        advanceUntilIdle()

        assertNotNull(vm.authState.value)
        vm.logout()

        assertNull(vm.authState.value)
        val s = vm.authUiState.value
        assertFalse(s.isLoading)
        assertFalse(s.isLoginSuccessful)
        assertNull(s.errorMessage)
        assertNull(s.successMessage)
        assertFalse(s.verificationEmailSent)
        assertFalse(s.blockNavigation)
    }

    @Test
    fun `google login success sets loginSuccessful`() = runTest(dispatcher) {
        authRepo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Success

        vm.loginWithGoogle("token")
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertTrue(s.isLoginSuccessful)
        assertNull(s.errorMessage)
        assertNotNull(vm.authState.value)
    }

    @Test
    fun `google login failure sets errorMessage`() = runTest(dispatcher) {
        authRepo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Error("INVALID_IDP_RESPONSE")

        vm.loginWithGoogle("token")
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertFalse(s.isLoginSuccessful)
        assertEquals("INVALID_IDP_RESPONSE", s.errorMessage)
        assertNull(vm.authState.value)
    }

    @Test
    fun `resendVerificationEmail updates state with error when no user`() = runTest(dispatcher) {
        // No hay usuario logueado
        vm.resendVerificationEmail()
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertFalse(s.verificationEmailSent)
        // El fake repo falla con USER_NOT_FOUND si no hay user
        assertEquals("USER_NOT_FOUND", s.errorMessage)
        // Flags razonables según tu UI
        assertFalse(s.isLoading)
        assertFalse(s.isLoginSuccessful)
    }

    @Test
    fun `resendVerificationEmail updates state with success when user exists`() = runTest(dispatcher) {
        authRepo.seedLoggedUser(email = "a@b.com")
        authRepo.isEmailVerified = false
        authRepo.nextResendVerification = FakeAuthRepositoryImpl.Outcome.Success

        vm.resendVerificationEmail()
        advanceUntilIdle()

        val s = vm.authUiState.value
        assertTrue(s.verificationEmailSent)
        assertEquals("VERIFICATION_EMAIL_SENT", s.successMessage) // ✅ expect code
        assertNull(s.errorMessage)
        assertFalse(s.isLoading)
    }
}