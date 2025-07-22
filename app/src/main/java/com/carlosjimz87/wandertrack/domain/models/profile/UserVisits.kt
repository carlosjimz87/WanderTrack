package com.carlosjimz87.wandertrack.domain.models.profile

data class UserVisits(
    val countryCodes: Set<String> = emptySet(),
    val cities: Map<String, Set<String>>  = emptyMap()
)