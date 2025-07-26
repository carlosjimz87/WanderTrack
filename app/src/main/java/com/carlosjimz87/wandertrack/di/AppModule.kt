package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.data.repo.AuthRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.FirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.data.repo.MapRepositoryImpl
import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.managers.SessionManagerImpl
import com.carlosjimz87.wandertrack.managers.StoreManager
import com.carlosjimz87.wandertrack.managers.StylesManager
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.viewmodel.MapViewModel
import com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<MapRepository> { MapRepositoryImpl(androidContext()) }
    single<FirestoreRepository> { FirestoreRepositoryImpl(get()) }

    // Managers
    single { StoreManager(androidContext()) }
    single { StylesManager(androidContext()) }
    single<SessionManager> { SessionManagerImpl(get()) }

    // Viewmodels
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { (userId: String) ->
        MapViewModel(userId, get<MapRepository>(), get<FirestoreRepository>())
    }
    viewModel { ProfileViewModel(get(), get()) }
}