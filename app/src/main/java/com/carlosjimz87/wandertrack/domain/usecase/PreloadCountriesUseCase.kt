package com.carlosjimz87.wandertrack.domain.usecase

import com.carlosjimz87.wandertrack.domain.models.map.Country
import com.carlosjimz87.wandertrack.domain.repo.CountriesBootstrapRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PreloadCountriesUseCase(
    private val repo: CountriesBootstrapRepository,
    private val io: CoroutineDispatcher
) {
    suspend operator fun invoke(): List<Country> = withContext(io) {
        repo.readCountriesFromRaw()
    }
}