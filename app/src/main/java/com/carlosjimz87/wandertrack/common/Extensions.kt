package com.carlosjimz87.wandertrack.common

import com.carlosjimz87.wandertrack.domain.models.City

fun List<City>.visited(): List<City> = this.filter { it.visited }.toList()