package com.carlosjimz87.wandertrack.common

import com.carlosjimz87.wandertrack.BuildConfig

/**
 * Application configuration for Firebase environment and emulator settings.
 */
object AppConfig {
    val isDev = BuildConfig.FIREBASE_ENV == "dev"
    val isProd = BuildConfig.FIREBASE_ENV == "prod"
    val useEmulator = BuildConfig.USE_FIRESTORE_EMULATOR

    val emulatorHost ="10.0.2.2"
    val authEmulatorPort = 9099
    val firestoreEmulatorPort = 8080

    val usersCollection: String
        get() = if (isDev) "users_dev" else "users"
}