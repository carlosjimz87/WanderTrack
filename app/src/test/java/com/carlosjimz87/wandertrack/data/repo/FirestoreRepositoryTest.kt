package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.data.repo.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country
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
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreRepositoryTest {

    private lateinit var repo: FakeFirestoreRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeFirestoreRepositoryImpl()

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

    @Test
    fun `ensureUserDocument initializes user data structures`() = runTest {
        val userId = "newUser"
        repo.ensureUserDocument(userId)

        val visits = repo.fetchUserVisits(userId)
        assertTrue(visits.countryCodes.isEmpty())
        assertTrue(visits.cities.isEmpty())
    }

    @Test
    fun `deleteUserDocument removes all user data`() = runTest {
        val userId = "testUser"
        repo.updateCountryVisited(userId, "IT", true)
        repo.updateCityVisited(userId, "IT", "Rome", true)

        repo.deleteUserDocument(userId)

        val countries = repo.fetchAllCountries(userId)
        val visits = repo.fetchUserVisits(userId)
        val profile = repo.fetchUserProfile(userId)

        assertFalse(countries.first().visited)
        assertTrue(visits.countryCodes.isEmpty())
        assertTrue(visits.cities.isEmpty())
        assertNull(profile)
    }

    @Test
    fun `fetchUserVisits returns correct country and city visits`() = runTest {
        val userId = "testUser"
        repo.updateCountryVisited(userId, "IT", true)
        repo.updateCityVisited(userId, "IT", "Rome", true)

        val visits = repo.fetchUserVisits(userId)

        assertEquals(setOf("IT"), visits.countryCodes)
        assertEquals(setOf("Rome"), visits.cities["IT"])
    }

    @Test
    fun `fetchUserProfile returns expected stats after updates`() = runTest {
        val userId = "testUser"
        repo.updateCountryVisited(userId, "IT", true)
        repo.updateCityVisited(userId, "IT", "Rome", true)

        val profile = repo.fetchUserProfile(userId)
        assertNotNull(profile)
        assertEquals(1, profile.countriesVisited)
        assertEquals(1, profile.citiesVisited)
        assertEquals(1, profile.continentsVisited)
        assertTrue(profile.worldPercent > 0)
    }

    @Test
    fun `unmarking country and city updates stats and visit list`() = runTest {
        val userId = "testUser"
        repo.updateCountryVisited(userId, "IT", true)
        repo.updateCityVisited(userId, "IT", "Rome", true)

        repo.updateCountryVisited(userId, "IT", false)
        repo.updateCityVisited(userId, "IT", "Rome", false)

        val visits = repo.fetchUserVisits(userId)
        val profile = repo.fetchUserProfile(userId)

        assertFalse("IT" in visits.countryCodes)
        assertTrue(visits.cities["IT"] == null || visits.cities["IT"]!!.isEmpty())
        assertEquals(0, profile?.countriesVisited)
        assertEquals(0, profile?.citiesVisited)
    }
}