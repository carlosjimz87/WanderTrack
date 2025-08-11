package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var repo: FakeAuthRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepositoryImpl()
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial currentUser is null`() = runTest {
        assertNull(repo.currentUser)
    }

    @Test
    fun `login success sets currentUser`() = runTest {
        repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success

        val r = repo.loginWithEmail("test@test.com", "password")
        assertTrue(r.isSuccess)
        assertNotNull(repo.currentUser)
    }

    @Test
    fun `login failure does not set currentUser`() = runTest {
        repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Error("WRONG_PASSWORD")

        val r = repo.loginWithEmail("test@test.com", "password")
        assertTrue(r.isFailure)
        assertNull(repo.currentUser)
    }

    @Test
    fun `signup success returns message and sets currentUser`() = runTest {
        repo.nextSignup = FakeAuthRepositoryImpl.Outcome.Success

        val r = repo.signup("new@test.com", "password")
        assertTrue(r.isSuccess)
        assertEquals("SIGNUP_OK_NEEDS_VERIFICATION", r.getOrNull())
        assertNotNull(repo.currentUser)
    }

    @Test
    fun `logout clears currentUser`() = runTest {
        repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success
        repo.loginWithEmail("test@test.com", "password")
        assertNotNull(repo.currentUser)

        repo.logout()
        assertNull(repo.currentUser)
        assertTrue(repo.logoutCalled)
    }

    @Test
    fun `login fails if email not verified`() = runTest {
        repo.isEmailVerified = false
        repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success // crea user pero no verificado

        val r = repo.loginWithEmail("test@test.com", "password")
        assertTrue(r.isFailure)
        assertEquals("EMAIL_NOT_VERIFIED", (r.exceptionOrNull() as Exception).message)
    }

    @Test
    fun `isUserLoggedIn reflects user presence and verification`() = runTest {
        assertFalse(repo.isUserLoggedIn())

        repo.isEmailVerified = true
        repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success
        val res = repo.loginWithEmail("test@test.com", "password")
        assertTrue(res.isSuccess)
        assertTrue(repo.isUserLoggedIn())

        repo.isEmailVerified = false
        assertFalse(repo.isUserLoggedIn())
    }

    @Test
    fun `resendVerificationEmail requires user`() = runTest {
        val r1 = repo.resendVerificationEmail()
        assertTrue(r1.isFailure)
        assertEquals("USER_NOT_FOUND", (r1.exceptionOrNull() as Exception).message)

        repo.seedLoggedUser(email = "a@b.com")
        repo.nextResendVerification = FakeAuthRepositoryImpl.Outcome.Success
        val r2 = repo.resendVerificationEmail()
        assertTrue(r2.isSuccess)
        assertEquals("VERIFICATION_EMAIL_SENT", r2.getOrNull())
        assertTrue(repo.resendVerificationCalled)
    }

    @Test
    fun `sendPasswordResetEmail success and trace`() = runTest {
        val r = repo.sendPasswordResetEmail("test@test.com")
        assertTrue(r.isSuccess)
        assertEquals("test@test.com", repo.lastResetEmail)
    }

    @Test
    fun `sendPasswordResetEmail error when configured`() = runTest {
        repo.nextPasswordReset = FakeAuthRepositoryImpl.Outcome.Error("TOO_MANY_REQUESTS")

        val r = repo.sendPasswordResetEmail("fail@test.com")
        assertTrue(r.isFailure)
        assertEquals("TOO_MANY_REQUESTS", (r.exceptionOrNull() as Exception).message)
        assertEquals("fail@test.com", repo.lastResetEmail)
    }

    @Test
    fun `loginWithGoogle success sets currentUser`() = runTest {
        repo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Success
        val r = repo.loginWithGoogle("dummy_token")
        assertTrue(r.isSuccess)
        assertNotNull(repo.currentUser)
        assertEquals("dummy_token", repo.lastGoogleIdToken)
    }

    @Test
    fun `loginWithGoogle failure when configured`() = runTest {
        repo.nextGoogleLogin = FakeAuthRepositoryImpl.Outcome.Error("INVALID_IDP_RESPONSE")
        val r = repo.loginWithGoogle("dummy_token")
        assertTrue(r.isFailure)
        assertNull(repo.currentUser)
        assertEquals("dummy_token", repo.lastGoogleIdToken)
    }

    @Test
    fun `email login error table covers common Firebase codes`() = runTest {
        val cases = listOf(
            "WRONG_PASSWORD",
            "USER_NOT_FOUND",
            "EMAIL_ALREADY_IN_USE",
            "ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
            "TOO_MANY_REQUESTS",
            "NETWORK_REQUEST_FAILED",
            "INTERNAL_ERROR"
        )
        for (code in cases) {
            repo.reset()
            repo.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Error(code)
            val r = repo.loginWithEmail("a@b.com", "x")
            assertTrue("$code should fail", r.isFailure)
            assertEquals(code, (r.exceptionOrNull() as Exception).message)
            assertNull(repo.currentUser)
        }
    }
}