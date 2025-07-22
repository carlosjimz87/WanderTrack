package com.carlosjimz87.wandertrack.domain.models

sealed class Screens {
    object Splash : Screens()
    object Auth : Screens()
    object Login : Screens()
    object SignUp : Screens()
    data class Map(val userId: String) : Screens()
    object Profile : Screens()

    fun toRouteString(): String = when (this) {
        is Splash -> "Splash"
        is Auth -> "Auth"
        is Login -> "Login"
        is SignUp -> "SignUp"
        is Map -> "Map"
        is Profile -> "Profile"
    }

    companion object {
        fun fromRouteString(route: String, userId: String? = null): Screens = when (route) {
            "Splash" -> Splash
            "Auth" -> Auth
            "Login" -> Login
            "SignUp" -> SignUp
            "Map" -> Map(userId ?: "")
            "Profile" -> Profile
            else -> Splash
        }
    }
}