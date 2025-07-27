package com.carlosjimz87.wandertrack.domain.models.map

data class Country(
    val code: String,
    val name: String,
    val continent: String,
    val visited: Boolean = false,
    val cities : List<City> = emptyList()
)