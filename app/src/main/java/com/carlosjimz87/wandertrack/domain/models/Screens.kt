package com.carlosjimz87.wandertrack.domain.models

sealed class Screens(val order: Int) {
    object Splash : Screens(0)
    object Auth : Screens(1)
    object Login : Screens(2)
    object SignUp : Screens(3)
    object Map : Screens(4)
    object Profile : Screens(5)

    fun isProtected(): Boolean = this is Map || this is Profile
    fun isPublic(): Boolean = this is Login || this is Auth || this is SignUp || this is Splash
}