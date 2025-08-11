package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeSessionManagerImpl
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.managers.SessionManagerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Run same assertions on any SessionManager impl
    private fun runSuite(makeManager: (FakeAuthRepositoryImpl) -> SessionManager) =
        runTest(dispatcher) {
            val auth = FakeAuthRepositoryImpl().apply { isEmailVerified = true }
            val sm = makeManager(auth)

            // Initial snapshot: no user -> false (or null if you prefer; adapt as needed)
            // If your impl initializes to null, change to assertNull.
            assertEquals(false, sm.validSession.value)

            // Login success -> true
            auth.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success
            val loginRes = auth.loginWithEmail("user@test.com", "pass")
            assertTrue(loginRes.isSuccess)
            // Listener path may be async; ensure it processes
            sm.refreshSession()
            advanceUntilIdle()
            assertEquals(true, sm.validSession.value)

            // Logout -> false
            auth.logout()
            sm.refreshSession()
            advanceUntilIdle()
            assertEquals(false, sm.validSession.value)

            // Not verified login → should remain false
            auth.isEmailVerified = false
            auth.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success
            val r = auth.loginWithEmail("nv@test.com", "p")
            assertTrue(r.isFailure) // fake returns EMAIL_NOT_VERIFIED
            sm.refreshSession()
            advanceUntilIdle()
            assertEquals(false, sm.validSession.value)

            // endSession() forces false
            auth.isEmailVerified = true
            auth.nextEmailLogin = FakeAuthRepositoryImpl.Outcome.Success
            assertTrue(auth.loginWithEmail("ok@test.com", "p").isSuccess)
            sm.refreshSession()
            advanceUntilIdle()
            assertEquals(true, sm.validSession.value)

            sm.endSession()
            advanceUntilIdle()
            assertEquals(false, sm.validSession.value)
        }

    @Test
    fun `SessionManagerImpl contract`() = runSuite { auth ->
        SessionManagerImpl(auth)
    }

    @Test
    fun `FakeSessionManagerImpl contract`() = runSuite { auth ->
        FakeSessionManagerImpl(auth)
    }
}