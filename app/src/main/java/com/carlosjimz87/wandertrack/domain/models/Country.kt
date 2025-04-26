package com.carlosjimz87.wandertrack.domain.models

data class Country(
    val code: String,
    val name: String,
    val visited: Boolean = false
)
