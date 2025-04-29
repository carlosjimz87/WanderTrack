package com.carlosjimz87.wandertrack.utils

import android.content.Context
import android.util.Log
import com.carlosjimz87.wandertrack.R
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
fun fetchCountriesGeoJson(context: Context): Map<String, List<List<LatLng>>> {
    return runCatching {
        context.resources.openRawResource(R.raw.countries).use { inputStream ->
            val geoJson = inputStream.bufferedReader().use { it.readText() }
            val parsed = JSONObject(geoJson)
            val features = parsed.getJSONArray("features")

            buildMap<String, List<List<LatLng>>> {
                for (i in 0 until features.length()) {
                    val feature = features.optJSONObject(i) ?: continue
                    val properties = feature.optJSONObject("properties") ?: continue
                    val geometry = feature.optJSONObject("geometry") ?: continue
                    val type = geometry.optString("type", "")
                    val coordinates = geometry.optJSONArray("coordinates") ?: continue

                    val isoCode = properties.optString("ISO_A2", "").uppercase()
                    val sovereign = properties.optString("SOVEREIGNT", "").uppercase()
                    val admin = properties.optString("ADMIN", "").uppercase()
                    val countryType = properties.optString("TYPE", "").uppercase()

                    // 🔴 Evitar ISO inválido
                    if (isoCode.isBlank() || isoCode == "-99") continue

                    // 🔴 Evitar pintar colonias (mismo ISO pero diferente ADMIN)
                    if (admin != sovereign && isoCode in setOf("FR", "GB", "US", "NL", "DK")) continue

                    val polygons = when (type) {
                        "Polygon" -> parsePolygon(coordinates)
                        "MultiPolygon" -> parseMultiPolygon(coordinates)
                        else -> emptyList()
                    }

                    if (polygons.isNotEmpty()) {
                        val existing = this[isoCode] ?: emptyList()
                        this[isoCode] = existing + polygons
                    }
                }
            }
        }
    }.getOrElse {
        it.printStackTrace()
        emptyMap()
    }
}

fun parsePolygon(coordinates: JSONArray): List<List<LatLng>> {
    val polygon = mutableListOf<LatLng>()
    val ring = coordinates.optJSONArray(0) ?: return emptyList()
    for (i in 0 until ring.length()) {
        val point = ring.optJSONArray(i)
        if (point != null && point.length() >= 2) {
            polygon.add(LatLng(point.getDouble(1), point.getDouble(0)))
        }
    }
    return listOf(polygon) // << retornamos como lista de polígonos
}

fun parseMultiPolygon(coordinates: JSONArray): List<List<LatLng>> {
    val polygons = mutableListOf<List<LatLng>>()

    for (i in 0 until coordinates.length()) {
        val polygonArray = coordinates.optJSONArray(i)?.optJSONArray(0) ?: continue
        val points = mutableListOf<LatLng>()

        for (j in 0 until polygonArray.length()) {
            val point = polygonArray.optJSONArray(j)
            if (point != null && point.length() >= 2) {
                points.add(LatLng(point.getDouble(1), point.getDouble(0)))
            }
        }

        if (points.isNotEmpty()) {
            polygons.add(points)
        }
    }

    return polygons
}

val countryNameToIso2 = mapOf(
    "FRANCE" to "FR",
    "UNITED STATES" to "US",
    "GERMANY" to "DE",
    "SPAIN" to "ES",
    "ITALY" to "IT",
)