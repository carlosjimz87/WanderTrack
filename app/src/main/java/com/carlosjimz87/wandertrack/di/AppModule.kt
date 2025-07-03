package com.carlosjimz87.wandertrack.di

import FirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.AuthRepository
import com.carlosjimz87.wandertrack.data.repo.AuthRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.data.repo.MapRepository
import com.carlosjimz87.wandertrack.data.repo.MapRepositoryImpl
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    // Repositories
    single<MapRepository> { MapRepositoryImpl(androidContext()) }
    single<FirestoreRepository> { FirestoreRepositoryImpl() }
    single<AuthRepository> { AuthRepositoryImpl() }

    viewModel { AuthViewModel(get(), get()) }

    viewModel { (userId: String) ->
        MapViewModel(userId, get<MapRepository>(), get<FirestoreRepository>())
    }
}