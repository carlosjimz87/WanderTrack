package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.domain.managers.SessionManager
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.carlosjimz87.wandertrack.domain.repo.FirestoreRepository
import com.carlosjimz87.wandertrack.domain.repo.MapRepository
import com.carlosjimz87.wandertrack.fakes.FakeAuthRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeFirestoreRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeMapRepositoryImpl
import com.carlosjimz87.wandertrack.fakes.FakeSessionManagerImpl
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.map.viewmodel.MapViewModel
import com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiTestsModule = module {
    // Fakes
    single<AuthRepository> { FakeAuthRepositoryImpl() }
    single<MapRepository> { FakeMapRepositoryImpl() }
    single<FirestoreRepository> { FakeFirestoreRepositoryImpl() }
    single<SessionManager> { FakeSessionManagerImpl(get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { (userId: String) -> MapViewModel(userId, get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
}