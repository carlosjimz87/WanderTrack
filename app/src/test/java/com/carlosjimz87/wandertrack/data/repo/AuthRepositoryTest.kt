package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.data.repo.fakes.FakeAuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class AuthRepositoryTest {

    private lateinit var repo: FakeAuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial currentUser is null`() = runTest {
        assertNull(repo.currentUser)
    }

    @Test
    fun `login success sets currentUser`() = runTest {
        var callbackSuccess = false

        repo.loginWithEmail("test@test.com", "password") { success, _ ->
            callbackSuccess = success
        }

        assertTrue(callbackSuccess)
        assertNotNull(repo.currentUser)
    }

    @Test
    fun `login failure does not set currentUser`() = runTest {
        repo.shouldFail = true
        var callbackSuccess = true

        repo.loginWithEmail("test@test.com", "password") { success, message ->
            callbackSuccess = success
            assertEquals("Login failed", message)
        }

        assertFalse(callbackSuccess)
        assertNull(repo.currentUser)
    }

    @Test
    fun `signup success sets currentUser`() = runTest {
        var callbackSuccess = false

        repo.signup("new@test.com", "password") { success, _ ->
            callbackSuccess = success
        }

        assertTrue(callbackSuccess)
        assertNotNull(repo.currentUser)
    }

    @Test
    fun `logout clears currentUser`() = runTest {
        repo.loginWithEmail("test@test.com", "password") { _, _ -> }
        assertNotNull(repo.currentUser)

        repo.logout()
        assertNull(repo.currentUser)
    }

    @Test
    fun `login fails if user email is not verified`() = runTest {
        repo.isEmailVerified = false
        var success = true
        var message: String? = null

        repo.loginWithEmail("test@test.com", "password") { s, m ->
            success = s
            message = m
        }

        assertFalse(success)
        assertEquals("Please verify your email before continuing.", message)
    }

    @Test
    fun `isUserLoggedIn returns true when user is logged in`() = runTest {
        repo.loginWithEmail("test@test.com", "password") { _, _ -> }
        assertTrue(repo.isUserLoggedIn())
    }

    @Test
    fun `isUserLoggedIn returns false when user is not logged in`() = runTest {
        assertFalse(repo.isUserLoggedIn())
    }

    @Test
    fun `resendVerificationEmail returns success if user exists`() = runTest {
        repo.loginWithEmail("test@test.com", "password") { _, _ -> }

        var success: Boolean? = null
        var message: String? = null
        repo.resendVerificationEmail { s, m ->
            success = s
            message = m
        }

        assertTrue(success!!)
        assertEquals("Verification email sent.", message)
        assertTrue(repo.resendVerificationCalled)
    }

    @Test
    fun `resendVerificationEmail returns error if no user`() = runTest {
        var success: Boolean? = null
        var message: String? = null

        repo.resendVerificationEmail { s, m ->
            success = s
            message = m
        }

        assertFalse(success!!)
        assertEquals("User not found.", message)
        assertTrue(repo.resendVerificationCalled)
    }

    @Test
    fun `resendVerificationEmail returns failure if flag set`() = runTest {
        repo.loginWithEmail("test@test.com", "password") { _, _ -> }
        repo.resendVerificationShouldFail = true

        var success: Boolean? = null
        var message: String? = null

        repo.resendVerificationEmail { s, m ->
            success = s
            message = m
        }

        assertFalse(success!!)
        assertEquals("Failed to send verification email.", message)
    }

    @Test
    fun `sendPasswordResetEmail returns success by default`() = runTest {
        var success: Boolean? = null
        var message: String? = null

        repo.sendPasswordResetEmail("test@test.com") { s, m ->
            success = s
            message = m
        }

        assertTrue(success!!)
        assertEquals("Password reset email sent.", message)
        assertEquals("test@test.com", repo.lastResetEmail)
    }

    @Test
    fun `sendPasswordResetEmail returns error when shouldResetPasswordFail is true`() = runTest {
        repo.shouldResetPasswordFail = true

        var success: Boolean? = null
        var message: String? = null

        repo.sendPasswordResetEmail("fail@test.com") { s, m ->
            success = s
            message = m
        }

        assertFalse(success!!)
        assertEquals("Failed to send password reset email.", message)
        assertEquals("fail@test.com", repo.lastResetEmail)
    }

    @Test
    fun `loginWithGoogle returns success if flag set`() = runTest {
        var success = false
        repo.setGoogleLoginResult(true)

        repo.loginWithGoogle("dummy_token") { s, _ -> success = s }

        assertTrue(success)
        assertNotNull(repo.currentUser)
        assertEquals("dummy_token", repo.lastGoogleIdToken)
    }

    @Test
    fun `loginWithGoogle returns failure if flag false`() = runTest {
        var success = true
        repo.setGoogleLoginResult(false)

        repo.loginWithGoogle("dummy_token") { s, _ -> success = s }

        assertFalse(success)
        assertNull(repo.currentUser)
        assertEquals("dummy_token", repo.lastGoogleIdToken)
    }

    @Test
    fun `signup delivers expected message`() = runTest {
        val result = CompletableDeferred<Pair<Boolean, String?>>()

        repo.signup("test@test.com", "pass") { s, m ->
            result.complete(s to m)
        }

        val (success, message) = result.await()
        assertTrue(success)
        assertTrue(message!!.contains("verification email"))
    }
}