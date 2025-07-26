package com.carlosjimz87.wandertrack.di

import com.carlosjimz87.wandertrack.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val firebaseModule = module {
    single<FirebaseAuth> {

        val auth = FirebaseAuth.getInstance()

        if (BuildConfig.FIREBASE_ENV == "dev") {
            val host = BuildConfig.FIREBASE_EMULATOR_HOST
            val port = BuildConfig.AUTH_EMULATOR_PORT
            auth.useEmulator(host, port)
        }

        auth
    }

    single<FirebaseFirestore> {

        val firestore = FirebaseFirestore.getInstance()

        if (BuildConfig.FIREBASE_ENV == "dev") {
            val host = BuildConfig.FIREBASE_EMULATOR_HOST
            val port = BuildConfig.STORE_EMULATOR_PORT
            firestore.useEmulator(host, port)
        }

        firestore
    }

}