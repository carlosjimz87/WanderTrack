package com.carlosjimz87.wandertrack.utils

import com.carlosjimz87.wandertrack.domain.models.Achievement
import com.carlosjimz87.wandertrack.domain.models.ProfileData
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProfileUiState(): ProfileData {
    val achievementsList = (get("achievements") as? List<Map<String, Any>>)?.map {
        Achievement(
            title = it["title"] as? String ?: "",
            description = it["desc"] as? String ?: ""
        )
    } ?: emptyList()

    return ProfileData(
        username = getString("username") ?: "",
        countriesVisited = (getLong("countries") ?: 0L).toInt(),
        citiesVisited = (getLong("cities") ?: 0L).toInt(),
        continentsVisited = (getLong("continents") ?: 0L).toInt(),
        worldPercent = (getLong("world") ?: 0L).toInt(),
        achievements = achievementsList
    )
}