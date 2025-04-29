package com.carlosjimz87.wandertrack.domain.models

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val visited: Boolean = false
)
