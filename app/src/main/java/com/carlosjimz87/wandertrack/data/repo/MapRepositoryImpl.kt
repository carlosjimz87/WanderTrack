package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLngOffline
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapRepositoryImpl(
    private val context: Context,
    private val geoProvider: (Context) -> Map<String, CountryGeometry> = ::fetchCountriesGeoJson,
    private val codeResolver: (Map<String, CountryGeometry>, LatLng) -> String? = ::getCountryCodeFromLatLngOffline
) : MapRepository {

    private val cachedGeometries: Map<String, CountryGeometry> by lazy {
        geoProvider(context)
    }

    private val cachedBounds: Map<String, LatLngBounds> by lazy {
        cachedGeometries.mapValues { (_, geometry) ->
            geometry.polygons.flatten().fold(LatLngBounds.builder()) { b, p -> b.include(p) }.build()
        }
    }

    override fun getCountryGeometries(): Map<String, CountryGeometry> = cachedGeometries

    override fun getCountryBounds(): Map<String, LatLngBounds> = cachedBounds

    override fun getCountryCodeFromLatLng(latLng: LatLng): String? =
        codeResolver(cachedGeometries, latLng)
}