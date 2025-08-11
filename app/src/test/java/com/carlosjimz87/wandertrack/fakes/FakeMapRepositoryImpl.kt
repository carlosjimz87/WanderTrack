package com.carlosjimz87.wandertrack.fakes

import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class FakeMapRepositoryImpl(
    private val customResolver: ((LatLng) -> String?)? = null
) : MapRepository {

    private val geometries = linkedMapOf<String, CountryGeometry>()
    private val bounds = linkedMapOf<String, LatLngBounds>()

    /** Remove all seeded data. */
    fun clear() {
        geometries.clear()
        bounds.clear()
    }

    /** Seed a country with a polygon; bounds are computed from the polygon points. */
    fun seedCountry(code: String, polygon: List<LatLng>) {
        val geom = CountryGeometry(polygons = listOf(polygon))
        geometries[code] = geom
        bounds[code] = polygon.fold(LatLngBounds.builder()) { b, p -> b.include(p) }.build()
    }

    /** Seed directly with a geometry (auto-compute bounds from all polygons). */
    fun seedCountry(code: String, geometry: CountryGeometry) {
        geometries[code] = geometry
        val b = LatLngBounds.builder()
        geometry.polygons.flatten().forEach { b.include(it) }
        bounds[code] = b.build()
    }

    /** Seed just a bounding box (quick & easy for UI tests). */
    fun seedBounds(code: String, bounds: LatLngBounds) {
        this.bounds[code] = bounds
        // Provide a degenerate rectangle polygon so callers that read geometries won't crash.
        val rect = listOf(
            LatLng(bounds.southwest.latitude, bounds.southwest.longitude),
            LatLng(bounds.northeast.latitude, bounds.southwest.longitude),
            LatLng(bounds.northeast.latitude, bounds.northeast.longitude),
            LatLng(bounds.southwest.latitude, bounds.northeast.longitude),
        )
        geometries[code] = CountryGeometry(polygons = listOf(rect))
    }

    /** Bulk seed geometries; bounds are computed. */
    fun seedGeometries(map: Map<String, CountryGeometry>) {
        map.forEach { (code, geom) -> seedCountry(code, geom) }
    }

    /** Bulk seed bounds (fast path). */
    fun seedBounds(map: Map<String, LatLngBounds>) {
        map.forEach { (code, b) -> seedBounds(code, b) }
    }

    // --- MapRepository impl ---

    override fun getCountryGeometries(): Map<String, CountryGeometry> = geometries.toMap()

    override fun getCountryBounds(): Map<String, LatLngBounds> = bounds.toMap()

    override fun getCountryCodeFromLatLng(latLng: LatLng): String? {
        // If caller provided a custom resolver, use it.
        customResolver?.invoke(latLng)?.let { return it }

        // Simple, fast containment: pick the first bounds that contains the point.
        // Good enough for tests; no heavy point-in-polygon math needed.
        return bounds.entries.firstOrNull { (_, b) ->
            b.contains(latLng)
        }?.key
    }
}