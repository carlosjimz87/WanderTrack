package com.carlosjimz87.wandertrack.domain.repo

import com.carlosjimz87.wandertrack.domain.models.map.Country

interface CountriesBootstrapRepository {
    fun readCountriesFromRaw(): List<Country>
}