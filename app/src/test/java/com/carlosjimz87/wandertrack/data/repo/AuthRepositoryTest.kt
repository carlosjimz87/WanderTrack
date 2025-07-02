package com.carlosjimz87.wandertrack.data.repo

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

        repo.login("test@test.com", "password") { success, _ ->
            callbackSuccess = success
        }

        assertTrue(callbackSuccess)
        assertNotNull(repo.currentUser)
    }

    @Test
    fun `login failure does not set currentUser`() = runTest {
        repo.shouldFail = true
        var callbackSuccess = true

        repo.login("test@test.com", "password") { success, message ->
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
        repo.login("test@test.com", "password") { _, _ -> }
        assertNotNull(repo.currentUser)

        repo.logout()
        assertNull(repo.currentUser)
    }
}