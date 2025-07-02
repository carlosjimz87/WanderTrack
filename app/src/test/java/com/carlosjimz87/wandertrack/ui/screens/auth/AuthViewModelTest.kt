package com.carlosjimz87.wandertrack.ui.screens.auth

import com.carlosjimz87.wandertrack.data.repo.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class AuthViewModelTest {

    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
        viewModel = AuthViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates authState`() = runTest {
        viewModel.login("test@test.com", "password") { success, _ ->
            assertTrue(success)
        }
        assertNotNull(viewModel.authState.first())
    }

    @Test
    fun `login failure does not update authState`() = runTest {
        repo.shouldFail = true

        viewModel.login("test@test.com", "password") { success, message ->
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
        viewModel.login("test@test.com", "password") { _, _ -> }
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
}