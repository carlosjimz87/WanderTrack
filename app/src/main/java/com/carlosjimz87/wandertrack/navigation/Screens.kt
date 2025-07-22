package com.carlosjimz87.wandertrack.navigation

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Login : Screen()
    object SignUp : Screen()
    data class Map(val userId: String) : Screen()
    object Profile : Screen()
}