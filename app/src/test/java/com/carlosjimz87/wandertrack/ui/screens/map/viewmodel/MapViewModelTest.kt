@file:OptIn(ExperimentalCoroutinesApi::class)

package com.carlosjimz87.wandertrack.ui.screens.map.viewmodel

import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.domain.usecase.GetCountriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.GetCountryGeometriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCityVisitedUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCountryVisitedUseCase
import com.carlosjimz87.wandertrack.utils.MainDispatcherRule
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class MapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MapViewModel
    private lateinit var getCountriesUseCase: GetCountriesUseCase
    private lateinit var getCountryGeometriesUseCase: GetCountryGeometriesUseCase
    private lateinit var updateCountryVisitedUseCase: UpdateCountryVisitedUseCase
    private lateinit var updateCityVisitedUseCase: UpdateCityVisitedUseCase
    private lateinit var mapRepository: MapRepository

    @Before
    fun setup() {
        getCountriesUseCase = mockk()
        getCountryGeometriesUseCase = mockk()
        updateCountryVisitedUseCase = mockk()
        updateCityVisitedUseCase = mockk()
        mapRepository = mockk(relaxed = true)

        coEvery { getCountriesUseCase.execute(any()) } returns listOf(
            Country(
                code = "US",
                name = "United States",
                continent = "North America",
                visited = true,
                cities = listOf(
                    City(
                        name = "New York",
                        latitude = 40.7128,
                        longitude = -74.0060,
                        visited = true
                    ),
                    City(
                        name = "Los Angeles",
                        latitude = 34.0522,
                        longitude = -118.2437,
                        visited = false
                    )
                )
            ),
            Country(
                code = "FR",
                name = "France",
                continent = "Europe",
                visited = false,
                cities = listOf(
                    City(name = "Paris", latitude = 48.8566, longitude = 2.3522, visited = false)
                )
            )
        )

        coEvery { getCountryGeometriesUseCase.execute() } returns mapOf(
            "US" to CountryGeometry(polygons = listOf(listOf(LatLng(0.0, 0.0)))),
            "FR" to CountryGeometry(polygons = listOf(listOf(LatLng(1.0, 1.0))))
        )

        viewModel = MapViewModel(
            userId = "testUser",
            getCountriesUseCase = getCountriesUseCase,
            getCountryGeometriesUseCase = getCountryGeometriesUseCase,
            updateCountryVisitedUseCase = updateCountryVisitedUseCase,
            updateCityVisitedUseCase = updateCityVisitedUseCase,
            mapRepo = mapRepository
        )
    }

    @After
    fun drain() = runTest(mainDispatcherRule.testDispatcher) { advanceUntilIdle() }

    @Test
    fun `loadData sets countries and visited states correctly`() = runTest {
        val st = viewModel.uiState.first { !it.isLoading }
        advanceUntilIdle()

        assertEquals(2, st.countries.size)
        assertTrue("US" in st.visitedCountryCodes, "US debe estar visitado")
        assertFalse("FR" in st.visitedCountryCodes, "FR no debe estar visitado")
    }

    @Test
    fun `toggleCountryVisited adds and removes correctly`() = runTest {
        coEvery { updateCountryVisitedUseCase.execute(any(), any(), any()) } just Runs

        // Asegúrate de partir de estado estable
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleCountryVisited("FR")
        // La actualización de UI es optimista/sincrónica; persistencia es en IO
        val st1 = viewModel.uiState.value
        assertTrue("FR" in st1.visitedCountryCodes, "FR debe añadirse a visitados")

        viewModel.toggleCountryVisited("FR")
        val st2 = viewModel.uiState.value
        assertFalse("FR" in st2.visitedCountryCodes, "FR debe eliminarse de visitados")
    }

    @Test
    fun `toggleCityVisited updates visitedCities and country state`() = runTest {
        coEvery { updateCityVisitedUseCase.execute(any(), any(), any(), any()) } just Runs
        coEvery { updateCountryVisitedUseCase.execute(any(), any(), any()) } just Runs

        // Espera a fin de carga
        viewModel.uiState.first { !it.isLoading }

        // Marcar Paris visitada → FR debería pasar a visitado
        viewModel.toggleCityVisited("FR", "Paris")
        var st = viewModel.uiState.value

        assertTrue(
            "FR" in st.visitedCountryCodes,
            "FR debe marcarse como visitado por ciudad visitada",
        )
        val parisVisited = st.countries
            .first { it.code == "FR" }
            .cities.first { it.name == "Paris" }
            .visited
        assertTrue(parisVisited,"Paris debe estar visitada")

        // Desmarcar Paris → FR vuelve a no visitado
        viewModel.toggleCityVisited("FR", "Paris")
        st = viewModel.uiState.value
        assertFalse(
            "FR" in st.visitedCountryCodes,
            "FR debe salir de visitados al no quedar ciudades",
        )
    }

    @Test
    fun `notifyUserMovedMap sets camera position`() = runTest {
        viewModel.uiState.first { !it.isLoading }

        val position = CameraPosition(LatLng(0.0, 0.0), 5f, 0f, 0f)
        viewModel.notifyUserMovedMap(position)
        val st = viewModel.uiState.value
        assertEquals(position, st.lastCameraPosition)
    }

    @Test
    fun `resolveCountryFromLatLng sets selectedCountry and returns bounds`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        advanceUntilIdle()

        val latLng = LatLng(0.0, 0.0)
        every { mapRepository.getCountryCodeFromLatLng(latLng) } returns "US"
        every { mapRepository.getCountryBounds() } returns mapOf(
            "US" to LatLngBounds(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        )

        val bounds = viewModel.resolveCountryFromLatLng(latLng)
        runCurrent() // drena cualquier post-actualización

        val st = viewModel.uiState.value
        assertEquals("US", st.selectedCountry?.code)
        assertNotNull(bounds)
    }

    @Test
    fun `getVisitedCountriesCenterAndBounds returns valid data`() = runTest {
        viewModel.uiState.first { !it.isLoading }
        advanceUntilIdle()

        every { mapRepository.getCountryGeometries() } returns mapOf(
            "US" to CountryGeometry(polygons = listOf(listOf(LatLng(0.0, 0.0)))),
            "FR" to CountryGeometry(polygons = listOf(listOf(LatLng(1.0, 1.0))))
        )

        val result = viewModel.getVisitedCountriesCenterAndBounds()
        advanceUntilIdle()

        assertNotNull(result)
        assertEquals(0.0, result!!.first.latitude, 0.001)
    }
}