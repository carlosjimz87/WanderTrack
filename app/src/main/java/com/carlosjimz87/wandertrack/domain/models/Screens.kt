package com.carlosjimz87.wandertrack.domain.models

sealed class Screens(val order: Int) {
    object Splash : Screens(0)
    object Auth : Screens(1)
    object Login : Screens(2)
    object SignUp : Screens(3)
    data class Map(val userId: String, val from: Source = Source.Default) : Screens(4) {
        enum class Source { Default, BackFromProfile }
    }
    object Profile : Screens(5)

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