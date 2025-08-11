package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirestoreRepositoryTest {

    private lateinit var repo: FakeFirestoreRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun country(
        code: String,
        name: String,
        cityNames: List<String> = emptyList()
    ) = Country(
        code = code,
        name = name,
        continent = "",           // not used by fake; it maps by code internally
        visited = false,
        cities = cityNames.map { City(name = it, latitude = 0.0, longitude = 0.0, visited = false) }
    )

    // Captured args for achievements
    private var lastAchArgs: Quad? = null
    private data class Quad(val c: Int, val ci: Int, val co: Int, val w: Int)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // achievementsCalc stub to capture arguments
        repo = FakeFirestoreRepositoryImpl { c, ci, co, w ->
            lastAchArgs = Quad(c, ci, co, w)
            emptyList() // keep it simple for tests
        }

        // 3 countries so continents & world% are testable (IT, BR, US supported by fake)
        val countries = listOf(
            country("IT", "Italy", listOf("Rome", "Milan")),
            country("BR", "Brazil", listOf("Rio", "São Paulo")),
            country("US", "United States", listOf("NYC", "LA"))
        )
        repo.setInitialData(countries)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Original-style happy-path tests (adapted) ---

    @Test
    fun `fetchCountries should return initial data`() = runTest {
        val result = repo.fetchAllCountries("testUser")
        assertEquals(3, result.size)
        assertEquals("Italy", result.first().name)
        assertFalse(result.first().visited)
    }

    @Test
    fun `updateCountryVisited should mark country as visited`() = runTest {
        repo.updateCountryVisited("testUser", "IT", true)
        val updated = repo.fetchAllCountries("testUser").first { it.code == "IT" }
        assertTrue(updated.visited)
    }

    @Test
    fun `updateCityVisited should mark city as visited`() = runTest {
        val userId = "testUser"
        repo.updateCityVisited(userId, "IT", "Rome", true)
        val rome = repo.fetchAllCountries(userId).first { it.code == "IT" }.cities.first { it.name == "Rome" }
        assertTrue(rome.visited)
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

        assertFalse(countries.first { it.code == "IT" }.visited)
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
        assertEquals(1, profile!!.countriesVisited)
        assertEquals(1, profile.citiesVisited)
        assertEquals(1, profile.continentsVisited) // IT => Europe
        // worldPercent = floor(1 / 3 * 100) = 33
        assertEquals(33, profile.worldPercent)
        // achievementsCalc args were called with same values
        assertEquals(Quad(1, 1, 1, 33), lastAchArgs)
    }

    @Test
    fun `unmarking country and city updates stats and visit list`() = runTest {
        val userId = "testUser"
        repo.updateCountryVisited(userId, "IT", true)
        repo.updateCityVisited(userId, "IT", "Rome", true)

        repo.updateCityVisited(userId, "IT", "Rome", false)
        repo.updateCountryVisited(userId, "IT", false)

        val visits = repo.fetchUserVisits(userId)
        val profile = repo.fetchUserProfile(userId)

        assertFalse("IT" in visits.countryCodes)
        assertTrue(visits.cities["IT"] == null || visits.cities["IT"]!!.isEmpty())
        assertEquals(0, profile?.countriesVisited)
        assertEquals(0, profile?.citiesVisited)
        assertEquals(0, profile?.continentsVisited)
        assertEquals(0, profile?.worldPercent)
        assertEquals(Quad(0, 0, 0, 0), lastAchArgs)
    }

    // --- New edge-case coverage ---

    @Test
    fun `updateCityVisited removes empty country entry when last city is unmarked`() = runTest {
        val userId = "u1"
        repo.updateCityVisited(userId, "IT", "Rome", true)
        repo.updateCityVisited(userId, "IT", "Rome", false)

        val visits = repo.fetchUserVisits(userId)
        assertNull("Country key should be gone when set empties", visits.cities["IT"])
    }

    @Test
    fun `ensureUserDocument is idempotent and does not clobber data`() = runTest {
        val userId = "u2"
        repo.seedVisited(userId, countries = setOf("IT"), cities = mapOf("IT" to setOf("Rome")))
        val before = repo.fetchUserVisits(userId)
        repo.ensureUserDocument(userId)
        val after = repo.fetchUserVisits(userId)
        assertEquals(before, after)
    }

    @Test
    fun `seedVisited populates data and stats correctly`() = runTest {
        val userId = "u3"
        repo.seedVisited(userId, countries = setOf("IT", "BR"), cities = mapOf("IT" to setOf("Rome")))
        val profile = repo.fetchUserProfile(userId)!!
        assertEquals(2, profile.countriesVisited)
        assertEquals(1, profile.citiesVisited)
        assertEquals(2, profile.continentsVisited) // IT(Europe) + BR(S. America)
        assertEquals(66, profile.worldPercent)     // floor(2/3*100)
        assertEquals(Quad(2, 1, 2, 66), lastAchArgs)
    }

    @Test
    fun `fake profile overrides computed stats`() = runTest {
        val userId = "u4"
        repo.seedVisited(userId, setOf("US"))
        val computed = repo.fetchUserProfile(userId)!!
        assertEquals(1, computed.countriesVisited)

        val fake = ProfileData(
            username = "Override",
            countriesVisited = 99,
            citiesVisited = 77,
            continentsVisited = 6,
            worldPercent = 100,
            achievements = emptyList()
        )
        repo.setFakeProfile(userId, fake)
        val overridden = repo.fetchUserProfile(userId)!!
        assertEquals("Override", overridden.username)
        assertEquals(99, overridden.countriesVisited)
    }

    @Test
    fun `multi-user isolation works`() = runTest {
        repo.updateCountryVisited("a", "IT", true)
        repo.updateCityVisited("a", "IT", "Rome", true)

        val bCountries = repo.fetchAllCountries("b")
        val italyForB = bCountries.first { it.code == "IT" }
        assertFalse("User b must not see user a's visits", italyForB.visited)
    }

    @Test
    fun `returned structures are copies and cannot mutate internal state`() = runTest {
        val userId = "u5"
        repo.updateCountryVisited(userId, "IT", true)
        val list = repo.fetchAllCountries(userId)
        // try to "mutate" by working with copies – repo should not change
        val hacked = list.first { it.code == "IT" }.copy(visited = false)
        assertFalse(hacked.visited) // local copy changed
        // repo state unchanged
        val italy = repo.fetchAllCountries(userId).first { it.code == "IT" }
        assertTrue(italy.visited)
        // Also ensure visits maps are defensive copies
        val visits = repo.fetchUserVisits(userId)
        val mutableSet = visits.countryCodes.toMutableSet()
        mutableSet.clear()
        val visitsAgain = repo.fetchUserVisits(userId)
        assertTrue("Repo must not be affected by external mutation", "IT" in visitsAgain.countryCodes)
    }

    @Test
    fun `stats handle empty metaCountries safely (worldPercent 0, no crash)`() = runTest {
        // Re-init with empty meta
        repo.setInitialData(emptyList())
        val userId = "z"
        repo.updateCountryVisited(userId, "IT", true) // IT not in meta, but method should still not crash
        val profile = repo.fetchUserProfile(userId)!!
        assertEquals(0, profile.worldPercent) // coerceAtLeast(1) -> denom=1, but cVisited=0 because IT not in meta
        assertEquals(0, profile.countriesVisited)
    }

    @Test
    fun `shouldFailFetch throws on fetches`() = runTest {
        repo.shouldFailFetch = true
        assertThrows(IllegalStateException::class.java) { runTest { repo.fetchAllCountries("u") } }
        assertThrows(IllegalStateException::class.java) { runTest { repo.fetchUserVisits("u") } }
        assertThrows(IllegalStateException::class.java) { runTest { repo.fetchUserProfile("u") } }
        repo.shouldFailFetch = false
    }

    @Test
    fun `shouldFailUpdate throws on updates`() = runTest {
        repo.shouldFailUpdate = true
        assertThrows(IllegalStateException::class.java) { runTest { repo.updateCountryVisited("u", "IT", true) } }
        assertThrows(IllegalStateException::class.java) { runTest { repo.updateCityVisited("u", "IT", "Rome", true) } }
        repo.shouldFailUpdate = false
    }

    @Test
    fun `shouldFailDelete throws on delete`() = runTest {
        val userId = "u6"
        repo.updateCountryVisited(userId, "IT", true)
        repo.shouldFailDelete = true
        assertThrows(IllegalStateException::class.java) { runTest { repo.deleteUserDocument(userId) } }
        repo.shouldFailDelete = false
        // After enabling delete again, it should work
        repo.deleteUserDocument(userId)
        val visits = repo.fetchUserVisits(userId)
        assertTrue(visits.countryCodes.isEmpty())
    }
}