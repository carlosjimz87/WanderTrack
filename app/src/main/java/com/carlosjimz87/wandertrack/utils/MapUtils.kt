package com.carlosjimz87.wandertrack.utils

import android.content.Context
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.Constants.countryNameToIso2
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.domain.models.CountryGeometry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONObject

fun fetchCountriesGeoJson(context: Context): Map<String, CountryGeometry> {
    return runCatching {
        val geoJson = context.resources
            .openRawResource(R.raw.simplified_borders)
            .bufferedReader()
            .use { it.readText() }

        val features = JSONObject(geoJson).getJSONArray("features")

        val result = mutableMapOf<String, CountryGeometry>()

        for (i in 0 until features.length()) {
            val feature = features.optJSONObject(i) ?: continue
            val properties = feature.optJSONObject("properties") ?: continue
            val geometry = feature.optJSONObject("geometry") ?: continue

            val isoCode = properties.optString("ISO_A2", "").uppercase()
            val sovereign = properties.optString("SOVEREIGNT", "").uppercase()
            val admin = properties.optString("ADMIN", "").uppercase()
            val type = geometry.optString("type", "")
            val coordinates = geometry.optJSONArray("coordinates") ?: continue

            if (!shouldIncludeCountry(isoCode, admin, sovereign, properties.optString("TYPE", ""))) continue

            val key = resolveCountryKey(isoCode, sovereign) ?: continue
            val rawPolygons = parseGeoJsonPolygons(type, coordinates)

            val filteredPolygons = filterOutOverseasTerritories(isoCode, rawPolygons)
            if (filteredPolygons.isEmpty()) continue

            val allPoints = filteredPolygons.flatten()
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }

            result[key] = CountryGeometry(polygons = filteredPolygons, bounds = boundsBuilder.build())
        }

        result
    }.getOrElse {
        it.printStackTrace()
        emptyMap()
    }
}

private fun shouldIncludeCountry(
    isoCode: String,
    admin: String,
    sovereign: String,
    countryType: String
): Boolean {
    if (isoCode == "-99") return false
    val isMainland = admin == sovereign && countryType.uppercase() == "COUNTRY"
    val knownColonizers = setOf("FRANCE", "UNITED KINGDOM", "UNITED STATES OF AMERICA", "NETHERLANDS", "DENMARK", "AUSTRALIA", "CHINA", "NORWAY")
    return isMainland || sovereign !in knownColonizers
}

private fun resolveCountryKey(isoCode: String, sovereign: String): String? {
    return if (isoCode.isNotBlank()) isoCode else countryNameToIso2[sovereign]
}

private fun parseGeoJsonPolygons(type: String, coordinates: JSONArray): List<List<LatLng>> {
    return when (type) {
        "Polygon" -> parsePolygon(coordinates)
        "MultiPolygon" -> parseMultiPolygon(coordinates)
        else -> emptyList()
    }
}

private fun parsePolygon(coordinates: JSONArray): List<List<LatLng>> {
    val ring = coordinates.optJSONArray(0) ?: return emptyList()
    val polygon = mutableListOf<LatLng>()
    for (i in 0 until ring.length()) {
        val point = ring.optJSONArray(i) ?: continue
        polygon.add(LatLng(point.getDouble(1), point.getDouble(0)))
    }
    return listOf(polygon)
}

private fun parseMultiPolygon(coordinates: JSONArray): List<List<LatLng>> {
    val polygons = mutableListOf<List<LatLng>>()
    for (i in 0 until coordinates.length()) {
        val ring = coordinates.optJSONArray(i)?.optJSONArray(0) ?: continue
        val polygon = mutableListOf<LatLng>()
        for (j in 0 until ring.length()) {
            val point = ring.optJSONArray(j) ?: continue
            polygon.add(LatLng(point.getDouble(1), point.getDouble(0)))
        }
        if (polygon.isNotEmpty()) polygons.add(polygon)
    }
    return polygons
}

private fun filterOutOverseasTerritories(code: String, polygons: List<List<LatLng>>): List<List<LatLng>> {
    if (code != "FR") return polygons
    return polygons.filter { polygon ->
        polygon.any { point ->
            point.latitude in 40.0..52.0 && point.longitude in -5.0..10.0
        }
    }
}

fun getCountryCodeFromLatLngOffline(
    borders: Map<String, CountryGeometry>,
    latLng: LatLng
): String? = borders.entries.firstOrNull { (_, geometry) ->
    geometry.polygons.any { polygon -> PolyUtil.containsLocation(latLng, polygon, true) }
}?.key

fun getCountryByCode(countries: List<Country>, code: String): Country? {
    return countries.find { it.code.equals(code, ignoreCase = true) }
}