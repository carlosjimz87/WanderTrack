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
        if (countriesVisited >= 50) achievements.add(Achievement("🎒", "50 countries explored"))
        if (countriesVisited >= 100) achievements.add(Achievement("🌐", "100 countries conquered"))
        if (countriesVisited >= 150) achievements.add(Achievement("🥇", "150 countries mastered"))
        if (countriesVisited >= 195) achievements.add(Achievement("👑", "Every country visited"))

        // Completed countries
        if (completedCountries >= 1) achievements.add(Achievement("🎯", "Completed a country"))
        if (completedCountries >= 5) achievements.add(Achievement("✅", "5 countries fully explored"))
        if (completedCountries >= 10) achievements.add(Achievement("🏁", "10 countries fully explored"))

        // Cities-based achievements
        if (citiesVisited >= 1) achievements.add(Achievement("🏙️", "First city visited"))
        if (citiesVisited >= 10) achievements.add(Achievement("🔟🏙️", "10 cities reached"))
        if (citiesVisited >= 50) achievements.add(Achievement("🏙️⭐", "50 cities explored"))
        if (citiesVisited >= 100) achievements.add(Achievement("🌃", "100 cities explored"))
        if (citiesVisited >= 250) achievements.add(Achievement("🌌", "250 cities explored"))
        if (citiesVisited >= 500) achievements.add(Achievement("🚀", "500 cities explored"))
        if (citiesVisited >= 1000) achievements.add(Achievement("🌍🏙️", "1000 cities explored"))

        // Continents-based achievements
        if (continentsVisited >= 1) achievements.add(Achievement("🌍", "First continent explored"))
        if (continentsVisited >= 3) achievements.add(Achievement("🌐", "3 continents explored"))
        if (completedContinents >= 1) achievements.add(Achievement("🗺️", "Completed a continent"))
        if (continentsVisited >= 5) achievements.add(Achievement("🌎", "5 continents explored"))
        if (continentsVisited >= 7) achievements.add(Achievement("🏆🌍", "All continents explored"))

        // World percent achievements
        if (worldPercent >= 5) achievements.add(Achievement("🎉", "5% of the world discovered"))
        if (worldPercent >= 10) achievements.add(Achievement("🚀", "10% of the world discovered"))
        if (worldPercent >= 25) achievements.add(Achievement("🗺️", "Quarter-world explorer"))
        if (worldPercent >= 50) achievements.add(Achievement("🌏", "Half the world explored"))
        if (worldPercent >= 75) achievements.add(Achievement("🏆", "75% of the world discovered"))
        if (worldPercent >= 100) achievements.add(Achievement("🌏👑", "Master of the world"))

        return achievements
    }
}