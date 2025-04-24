package com.carlosjimz87.wandertrack

import android.app.Application
import com.carlosjimz87.wandertrack.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.carlosjimz87.wandertrack.utils.Logger


class WanderTrackApp : Application(){
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        initializeKoin()
    }

    private fun initializeKoin(){
        startKoin {
            androidContext(this@WanderTrackApp)
            modules(appModule)
        }
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Logger.e("Error initializing Firebase [$e]")
        }
    }
}