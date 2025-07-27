package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.common.AppConfig
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val firebaseModule = module {

    single<FirebaseAuth> {
        val auth = FirebaseAuth.getInstance()

        if (AppConfig.isDev && AppConfig.useEmulator) {
            Logger.d("Firebase -> Using Auth Emulator at ${AppConfig.emulatorHost}:${AppConfig.authEmulatorPort}")
            auth.useEmulator(AppConfig.emulatorHost, AppConfig.authEmulatorPort)
        } else {
            Logger.d("Firebase -> Connecting to REAL Firebase Auth")
        }

        auth
    }

    single<FirebaseFirestore> {
        val firestore = FirebaseFirestore.getInstance()

        if (AppConfig.isDev && AppConfig.useEmulator) {
            Logger.d("Firebase -> Using Firestore Emulator at ${AppConfig.emulatorHost}:${AppConfig.firestoreEmulatorPort}")
            firestore.useEmulator(AppConfig.emulatorHost, AppConfig.firestoreEmulatorPort)
        } else {
            Logger.d("Firebase -> Connecting to REAL Firestore")
        }

        firestore
    }
}