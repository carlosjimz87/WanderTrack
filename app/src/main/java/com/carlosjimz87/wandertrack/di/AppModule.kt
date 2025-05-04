package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.data.repos.map.MapRepository
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // repositories
    single { MapRepository(androidContext()) }

    // viewmodels
    viewModel { AuthViewModel() }
    viewModel { MapViewModel(get()) }
}