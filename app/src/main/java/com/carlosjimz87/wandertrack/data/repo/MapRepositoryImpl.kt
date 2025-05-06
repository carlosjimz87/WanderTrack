package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson

class MapRepositoryImpl(private val context: Context) : MapRepository{
    override fun getCountryBorders(): Map<String, CountryGeometry> {
        return fetchCountriesGeoJson(context)
    }
}