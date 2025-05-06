package com.carlosjimz87.wandertrack.di

import FirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.data.repo.MapRepository
import com.carlosjimz87.wandertrack.data.repo.MapRepositoryImpl
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    // repositories
    single<MapRepository> { MapRepositoryImpl(androidContext()) }
    single<FirestoreRepository> { FirestoreRepositoryImpl() }

    // dummy userId provider (replace with real logic or injected service)
    factory<String>(qualifier = named("userId")) { "test_user_123" }

    // viewmodels
    viewModel { AuthViewModel() }
    viewModel {
        val userId: String = get(named("userId"))
        MapViewModel(userId, get<MapRepository>(), get<FirestoreRepository>())
    }
}