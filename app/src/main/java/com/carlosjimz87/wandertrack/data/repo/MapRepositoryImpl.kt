package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapRepositoryImpl(private val context: Context) : MapRepository{
    private var cachedBorders: Map<String, CountryGeometry>? = null

    override suspend fun getCountryBorders(): Map<String, CountryGeometry> {
        return cachedBorders ?: loadBorders().also { cachedBorders = it }
    }

    private suspend fun loadBorders(): Map<String, CountryGeometry> = withContext(Dispatchers.IO) {
        fetchCountriesGeoJson(context)
    }

    override fun clearCache() {
        cachedBorders = null
    }
}