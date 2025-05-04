package com.carlosjimz87.wandertrack.data.repos.map

import android.content.Context
import com.carlosjimz87.wandertrack.utils.fetchCountriesGeoJson
import com.google.android.gms.maps.model.LatLng

class MapRepository(private val context: Context) {
    fun getCountryBorders(): Map<String, List<List<LatLng>>> {
        return fetchCountriesGeoJson(context)
    }
}