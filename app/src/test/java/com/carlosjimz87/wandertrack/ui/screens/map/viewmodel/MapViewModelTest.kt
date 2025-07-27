package com.carlosjimz87.wandertrack.ui.screens.map.viewmodel

import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private lateinit var viewModel: MapViewModel
    private lateinit var mapRepository: MapRepository
    private lateinit var firestoreRepository: FirestoreRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mapRepository = mockk(relaxed = true)
        firestoreRepository = mockk(relaxed = true)

        coEvery { firestoreRepository.fetchAllCountries(userId = any()) } returns listOf(
            Country(
                code = "US",
                name = "United States",
                visited = true,
                cities = listOf(
                    City(name = "New York", latitude = 40.7128, longitude = -74.0060, visited = true),
                    City(name = "Los Angeles", latitude = 34.0522, longitude = -118.2437, visited = false)
                )
            ),
            Country(
                code = "FR",
                name = "France",
                visited = false,
                cities = listOf(
                    City(name = "Paris", latitude = 48.8566, longitude = 2.3522, visited = false)
                )
            )
        )

        coEvery { mapRepository.getCountryGeometries() } returns mapOf(
            "US" to CountryGeometry(polygons = listOf(listOf(LatLng(0.0, 0.0)))),
            "FR" to CountryGeometry(polygons = listOf(listOf(LatLng(1.0, 1.0))))
        )

        viewModel = MapViewModel(
            userId = "testUser",
            mapRepo = mapRepository,
            firestoreRepo = firestoreRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData sets countries and visited states correctly`() = runTest {
        assertEquals(2, viewModel.countries.value.size)
        assertTrue(viewModel.visitedCountryCodes.value.contains("US"))
        assertFalse(viewModel.visitedCountryCodes.value.contains("FR"))
    }

    @Test
    fun `toggleCountryVisited adds and removes correctly`() = runTest {
        coEvery { firestoreRepository.updateCountryVisited(any(), any(), any()) } just Runs
        viewModel.toggleCountryVisited("FR")
        runCurrent()
        assertTrue(viewModel.visitedCountryCodes.value.contains("FR"))

        viewModel.toggleCountryVisited("FR")
        runCurrent()
        assertFalse(viewModel.visitedCountryCodes.value.contains("FR"))
    }

    @Test
    fun `toggleCityVisited updates visitedCities and country state`() = runTest {
        coEvery { firestoreRepository.updateCityVisited(any(), any(), any(), any()) } just Runs
        coEvery { firestoreRepository.updateCountryVisited(any(), any(), any()) } just Runs

        viewModel.toggleCityVisited("FR", "Paris")
        runCurrent()
        assertTrue(viewModel.visitedCountryCodes.value.contains("FR"))
        assertTrue(viewModel.countries.value.find { it.code == "FR" }!!.cities.find { it.name == "Paris" }!!.visited)

        viewModel.toggleCityVisited("FR", "Paris")
        runCurrent()
        assertFalse(viewModel.visitedCountryCodes.value.contains("FR"))
    }

    @Test
    fun `notifyUserMovedMap sets flag and camera`() = runTest {
        val position = CameraPosition(LatLng(0.0, 0.0), 5f, 0f, 0f)
        viewModel.notifyUserMovedMap(position)
        assertEquals(position, viewModel.lastCameraPosition.value)
    }

    @Test
    fun `resolveCountryFromLatLng sets selectedCountry and returns bounds`() = runTest {
        val latLng = LatLng(0.0, 0.0)
        every { mapRepository.getCountryCodeFromLatLng(latLng) } returns "US"
        every { mapRepository.getCountryBounds() } returns mapOf(
            "US" to LatLngBounds(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        )

        val bounds = viewModel.resolveCountryFromLatLng(latLng)
        runCurrent()

        assertEquals("US", viewModel.selectedCountry.value?.code)
        assertNotNull(bounds)
    }

    @Test
    fun `getVisitedCountriesCenterAndBounds returns valid data`() = runTest {
        val result = viewModel.getVisitedCountriesCenterAndBounds()
        assertNotNull(result)
        assertEquals(0.0, result!!.first.latitude, 0.001)
    }
}