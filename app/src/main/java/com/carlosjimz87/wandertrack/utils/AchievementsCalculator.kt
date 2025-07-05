package com.carlosjimz87.wandertrack.utils

import com.carlosjimz87.wandertrack.domain.models.Achievement

object AchievementsCalculator {

    fun calculateAchievements(
        countriesVisited: Int,
        citiesVisited: Int,
        continentsVisited: Int,
        worldPercent: Int,
        completedCountries: Int = 0,
        completedContinents: Int = 0
    ): List<Achievement> {

        val achievements = mutableListOf<Achievement>()

        // Countries-based achievements
        if (countriesVisited >= 1) achievements.add(Achievement("🚩", "First country visited"))
        if (countriesVisited >= 5) achievements.add(Achievement("5️⃣", "5 countries reached"))
        if (countriesVisited >= 10) achievements.add(Achievement("🔟", "10 countries reached"))
        if (countriesVisited >= 20) achievements.add(Achievement("🌍", "20 countries reached"))

        // Completed countries
        if (completedCountries >= 1) achievements.add(Achievement("🎯", "Completed a country"))

        // Cities-based achievements
        if (citiesVisited >= 1) achievements.add(Achievement("🏙️", "First city visited"))
        if (citiesVisited >= 10) achievements.add(Achievement("🔟🏙️", "10 cities reached"))
        if (citiesVisited >= 50) achievements.add(Achievement("🏙️⭐", "50 cities explored"))

        // Continents-based achievements
        if (continentsVisited >= 1) achievements.add(Achievement("🌍", "First continent explored"))
        if (continentsVisited >= 3) achievements.add(Achievement("🌐", "3 continents explored"))
        if (completedContinents >= 1) achievements.add(Achievement("🗺️", "Completed a continent"))
        if (continentsVisited >= 7) achievements.add(Achievement("🌎", "Visited all continents"))

        // World percent achievements
        if (worldPercent >= 5) achievements.add(Achievement("🎉", "5% of the world discovered"))
        if (worldPercent >= 10) achievements.add(Achievement("🚀", "10% of the world discovered"))
        if (worldPercent >= 25) achievements.add(Achievement("🏆", "Quarter-world explorer"))

        return achievements
    }
}