package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val firebaseModule = module {
    single<FirebaseAuth> {
        val auth = FirebaseAuth.getInstance()

        if (BuildConfig.FIREBASE_ENV == "dev") {
            val host = BuildConfig.AUTH_EMULATOR_HOST
            val port = BuildConfig.AUTH_EMULATOR_PORT
            auth.useEmulator(host, port)
        }

        auth
    }
}