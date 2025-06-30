package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.CountryGeometry

interface MapRepository{
    suspend fun getCountryBorders(): Map<String, CountryGeometry>
    fun clearCache()
}