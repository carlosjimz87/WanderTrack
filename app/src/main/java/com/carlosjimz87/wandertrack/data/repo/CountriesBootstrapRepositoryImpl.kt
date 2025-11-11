package com.carlosjimz87.wandertrack.data.repo

import android.content.Context
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.repo.CountriesBootstrapRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CountriesBootstrapRepositoryImpl(
    private val appContext: Context
) : CountriesBootstrapRepository{
    override fun readCountriesFromRaw(): List<Country> {
        return runCatching {
            appContext.resources.openRawResource(R.raw.country_codes)
                .bufferedReader()
                .use { it.readText() }
        }.mapCatching { json ->
            val type = object : TypeToken<List<Country>>() {}.type
            Gson().fromJson<List<Country>>(json, type)
        }.getOrElse { emptyList() }
    }
}