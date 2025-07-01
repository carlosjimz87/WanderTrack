package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.carlosjimz87.wandertrack.utils.getCountryCodeFromLatLngOffline
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class MapRepositoryImpl(private val context: Context) : MapRepository {

    private val cachedGeometries: Map<String, CountryGeometry> by lazy {
        fetchCountriesGeoJson(context)
    }

    private val cachedBounds: Map<String, LatLngBounds> by lazy {
        cachedGeometries.mapValues { (_, geometry) ->
            geometry.polygons.flatten().fold(LatLngBounds.builder()) { builder, point ->
                builder.include(point)
            }.build()
        }
    }

    override fun getCountryGeometries(): Map<String, CountryGeometry> {
        return cachedGeometries
    }

    override fun getCountryBounds(): Map<String, LatLngBounds> {
        return cachedBounds
    }

    override fun getCountryCodeFromLatLng(latLng: LatLng): String? {
        return getCountryCodeFromLatLngOffline(cachedGeometries, latLng)
    }
}