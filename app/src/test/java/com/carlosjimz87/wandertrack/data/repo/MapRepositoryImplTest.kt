package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.mockk.mockk
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals


class MapRepositoryImplTest {

    private fun rect(lat1: Double, lon1: Double, lat2: Double, lon2: Double): List<LatLng> {
        val swLat = minOf(lat1, lat2); val swLon = minOf(lon1, lon2)
        val neLat = maxOf(lat1, lat2); val neLon = maxOf(lon1, lon2)
        return listOf(
            LatLng(swLat, swLon),
            LatLng(neLat, swLon),
            LatLng(neLat, neLon),
            LatLng(swLat, neLon),
        )
    }

    /** Simple resolver for tests: bounds containment over polygons. */
    private fun simpleResolver(geoms: Map<String, CountryGeometry>, p: LatLng): String? {
        geoms.forEach { (code, g) ->
            val b = LatLngBounds.builder().apply { g.polygons.flatten().forEach { include(it) } }.build()
            if (b.contains(p)) return code
        }
        return null
    }

    private fun makeRepo(geos: Map<String, CountryGeometry>): MapRepository {
        val ctx = mockk<Context>(relaxed = true)
        val provider: (Context) -> Map<String, CountryGeometry> = { geos }
        val resolver: (Map<String, CountryGeometry>, LatLng) -> String? = ::simpleResolver
        return MapRepositoryImpl(ctx, provider, resolver)
    }

    @Test
    fun `getCountryGeometries returns provided geometries`() {
        val itPoly = rect(36.0, 6.0, 47.0, 19.0)      // coarse Italy box
        val usPoly = rect(24.0, -125.0, 49.0, -66.0)  // coarse contiguous US box

        val geos = mapOf(
            "IT" to CountryGeometry(polygons = listOf(itPoly)),
            "US" to CountryGeometry(polygons = listOf(usPoly))
        )

        val repo = makeRepo(geos)

        val out = repo.getCountryGeometries()
        assertEquals(2, out.size)
        assertTrue("IT in geometries", out.containsKey("IT"))
        assertTrue("US in geometries", out.containsKey("US"))
        assertEquals(itPoly.size, out["IT"]!!.polygons.first().size)
    }

    @Test
    fun `getCountryBounds computes bounds from polygons`() {
        val brPoly = rect(-33.0, -73.0, 5.0, -34.0) // coarse Brazil
        val geos = mapOf("BR" to CountryGeometry(polygons = listOf(brPoly)))
        val repo = makeRepo(geos)

        val bounds = repo.getCountryBounds()
        assertEquals(1, bounds.size)
        val br = bounds["BR"]!!
        // Expect SW == (-33, -73), NE == (5, -34)
        assertEquals(-33.0, br.southwest.latitude, 1e-6)
        assertEquals(-73.0, br.southwest.longitude, 1e-6)
        assertEquals(5.0, br.northeast.latitude, 1e-6)
        assertEquals(-34.0, br.northeast.longitude, 1e-6)
    }

    @Test
    fun `getCountryCodeFromLatLng resolves point inside bounds`() {
        val itPoly = rect(36.0, 6.0, 47.0, 19.0)
        val usPoly = rect(24.0, -125.0, 49.0, -66.0)
        val geos = mapOf(
            "IT" to CountryGeometry(polygons = listOf(itPoly)),
            "US" to CountryGeometry(polygons = listOf(usPoly))
        )
        val repo = makeRepo(geos)

        // Inside Italy
        val romeish = LatLng(41.9, 12.5)
        assertEquals("IT", repo.getCountryCodeFromLatLng(romeish))

        // Inside US
        val nycish = LatLng(40.7, -74.0)
        assertEquals("US", repo.getCountryCodeFromLatLng(nycish))

        // Ocean
        val ocean = LatLng(0.0, -10.0)
        assertNull(repo.getCountryCodeFromLatLng(ocean))
    }

    @Test
    fun `lazy caches are stable across calls`() {
        val itPoly = rect(36.0, 6.0, 47.0, 19.0)
        val geos = mapOf("IT" to CountryGeometry(polygons = listOf(itPoly)))
        val repo = makeRepo(geos)

        val g1 = repo.getCountryGeometries()
        val g2 = repo.getCountryGeometries()
        assertTrue(g1 === g2) // same instance from lazy

        val b1 = repo.getCountryBounds()
        val b2 = repo.getCountryBounds()
        assertTrue(b1 === b2)
    }
}