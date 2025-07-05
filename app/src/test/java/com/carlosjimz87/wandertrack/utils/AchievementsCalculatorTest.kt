package com.carlosjimz87.wandertrack.utils

import org.junit.jupiter.api.Assertions.*

import org.junit.Test

class AchievementsCalculatorTest {

    @Test
    fun `should return first country achievement when 1 country visited`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 1,
            citiesVisited = 0,
            continentsVisited = 0,
            worldPercent = 0
        )

        assertTrue(achievements.any { it.title == "🚩" })
    }

    @Test
    fun `should return correct achievements for 10 countries visited`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 10,
            citiesVisited = 0,
            continentsVisited = 0,
            worldPercent = 0
        )

        assertTrue(achievements.any { it.title == "🚩" })
        assertTrue(achievements.any { it.title == "5️⃣" })
        assertTrue(achievements.any { it.title == "🔟" })
    }

    @Test
    fun `should return completed country achievement`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 10,
            citiesVisited = 0,
            continentsVisited = 0,
            worldPercent = 0,
            completedCountries = 1
        )

        assertTrue(achievements.any { it.title == "🎯" })
    }

    @Test
    fun `should return city achievements`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 0,
            citiesVisited = 50,
            continentsVisited = 0,
            worldPercent = 0
        )

        assertTrue(achievements.any { it.title == "🏙️" })
        assertTrue(achievements.any { it.title == "🔟🏙️" })
        assertTrue(achievements.any { it.title == "🏙️⭐" })
    }

    @Test
    fun `should return continent achievements`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 0,
            citiesVisited = 0,
            continentsVisited = 7,
            worldPercent = 0,
            completedContinents = 1
        )

        assertTrue(achievements.any { it.title == "🌍" })
        assertTrue(achievements.any { it.title == "🌐" })
        assertTrue(achievements.any { it.title == "🗺️" })
        assertTrue(achievements.any { it.title == "🌎" })
    }

    @Test
    fun `should return world percent achievements`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 0,
            citiesVisited = 0,
            continentsVisited = 0,
            worldPercent = 25
        )

        assertTrue(achievements.any { it.title == "🎉" })
        assertTrue(achievements.any { it.title == "🚀" })
        assertTrue(achievements.any { it.title == "🏆" })
    }

    @Test
    fun `should return empty list for no progress`() {
        val achievements = AchievementsCalculator.calculateAchievements(
            countriesVisited = 0,
            citiesVisited = 0,
            continentsVisited = 0,
            worldPercent = 0
        )

        assertTrue(achievements.isEmpty())
    }
}