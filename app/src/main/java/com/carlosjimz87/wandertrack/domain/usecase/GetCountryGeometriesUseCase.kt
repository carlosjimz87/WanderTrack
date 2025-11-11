package com.carlosjimz87.wandertrack.domain.usecase

import com.carlosjimz87.wandertrack.domain.models.map.CountryGeometry
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCountryGeometriesUseCase(private val mapRepo: MapRepository) {
    suspend fun execute(): Map<String, CountryGeometry> = withContext(Dispatchers.IO) {
        mapRepo.getCountryGeometries()
    }
}