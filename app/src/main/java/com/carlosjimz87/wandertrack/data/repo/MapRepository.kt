package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.domain.models.CountryGeometry

interface MapRepository{
    fun getCountryBorders(): Map<String, CountryGeometry>
}