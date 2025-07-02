package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country
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
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreRepositoryTest {

    private lateinit var repo: FakeFirestoreRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeFirestoreRepository()

        val countries = listOf(
            Country(
                code = "IT",
                name = "Italy",
                visited = false,
                cities = listOf(
                    City(name = "Rome", latitude = 1.0, longitude = 1.0, visited = false)
                )
            )
        )
        repo.setInitialData(countries)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCountries should return initial data`() = runTest {
        val result = repo.fetchAllCountries("testUser")
        assertEquals(1, result.size)
        assertEquals("Italy", result.first().name)
    }

    @Test
    fun `updateCountryVisited should mark country as visited`() = runTest {
        repo.updateCountryVisited("testUser", "IT", true)
        val updated = repo.fetchAllCountries("testUser").first()
        assertTrue(updated.visited)
    }

    @Test
    fun `updateCityVisited should mark city as visited`() = runTest {
        val userId = "testUser"
        repo.updateCityVisited(userId, "IT", "Rome", true)
        val updated = repo.fetchAllCountries(userId).first().cities.first()
        assertTrue(updated.visited)
    }
}