package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.domain.profile.usecase.GetProfileDataUseCase
import com.carlosjimz87.wandertrack.domain.usecase.GetCountriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.GetCountryGeometriesUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCountryVisitedUseCase
import com.carlosjimz87.wandertrack.domain.usecase.UpdateCityVisitedUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { GetCountriesUseCase(get()) }
    single { GetCountryGeometriesUseCase(get()) }
    single { UpdateCountryVisitedUseCase(get()) }
    single { UpdateCityVisitedUseCase(get()) }
    factory { GetProfileDataUseCase(get(), get()) }
}
