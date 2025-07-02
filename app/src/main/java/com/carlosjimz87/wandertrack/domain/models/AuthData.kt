package com.carlosjimz87.wandertrack.domain.models

sealed class AuthData {
    data class Email(val email: String, val password: String) : AuthData()
    data class Google(val idToken: String) : AuthData()
}