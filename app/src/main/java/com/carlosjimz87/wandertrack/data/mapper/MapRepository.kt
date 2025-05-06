package com.carlosjimz87.wandertrack.data.mapper

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson

class MapRepository(private val context: Context) {
    fun getCountryBorders(): Map<String, CountryGeometry> {
        return fetchCountriesGeoJson(context)
    }
}