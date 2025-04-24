package com.carlosjimz87.wandertrack.di

import org.koin.dsl.module

val appModule = module {
    single<String> { "Hola" }
}