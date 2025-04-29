package com.carlosjimz87.wandertrack.utils

import android.content.Context
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.Country
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONObject


fun fetchCountriesGeoJson(context: Context): Map<String, List<List<LatLng>>> {
    return runCatching {
        context.resources.openRawResource(R.raw.simplified_borders).use { inputStream ->
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

                    if (isoCode == "-99") continue

                    // Filtro de colonias para países conocidos
                    val isMainland = admin == sovereign && countryType == "COUNTRY"
                    if (!isMainland && sovereign in setOf(
                            "FRANCE", "UNITED KINGDOM", "UNITED STATES OF AMERICA",
                            "NETHERLANDS", "DENMARK", "AUSTRALIA", "CHINA", "NORWAY"
                        )
                    ) continue

                    // Obtener la clave válida
                    val key = when {
                        isoCode.isNotBlank() -> isoCode
                        isMainland -> countryNameToIso2[sovereign] ?: continue
                        else -> continue
                    }

                    val polygons = when (type) {
                        "Polygon" -> parsePolygon(coordinates)
                        "MultiPolygon" -> parseMultiPolygon(coordinates)
                        else -> emptyList()
                    }

                    // Filtro específico para evitar territorios de ultramar (por ejemplo, Guayana Francesa)
                    val filteredPolygons = if (isoCode == "FR") {
                        polygons.filter { polygon ->
                            polygon.any { point ->
                                point.latitude in 40.0..52.0 && point.longitude in -5.0..10.0
                            }
                        }
                    } else {
                        polygons
                    }

                    if (filteredPolygons.isNotEmpty()) {
                        val existing = this[key] ?: emptyList()
                        this[key] = existing + filteredPolygons
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

fun getCountryByCode(countries: List<Country>, code: String): Country? {
    return countries.find { it.code.equals(code, ignoreCase = true) }
}

fun getCountryCodeFromLatLngOffline(borders: Map<String, List<List<LatLng>>>, latLng: LatLng): String? {
    for ((code, polygons) in borders) {
        for (polygon in polygons) {
            if (PolyUtil.containsLocation(latLng, polygon, true)) {
                return code
            }
        }
    }
    return null
}