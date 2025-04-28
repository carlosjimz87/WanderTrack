package com.carlosjimz87.wandertrack.domain.models

data class City(
    val code: String,
    val name: String,
    val visited: Boolean = false,
)
