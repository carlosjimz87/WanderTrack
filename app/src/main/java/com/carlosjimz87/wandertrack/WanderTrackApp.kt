package com.carlosjimz87.wandertrack

import android.app.Application
import com.carlosjimz87.wandertrack.di.appModule
import com.carlosjimz87.wandertrack.di.firebaseModule
import com.carlosjimz87.wandertrack.managers.StylesManager
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class WanderTrackApp : Application(){
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        initializeKoin()
    }

    companion object {
        lateinit var instance: WanderTrackApp
            private set
    }

    private fun initializeKoin(){
        startKoin {
            androidContext(this@WanderTrackApp)
            modules(listOf(appModule, firebaseModule))
        }
        get<StylesManager>().preloadStyles()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
           // FirebaseFirestore.setLoggingEnabled(true)
        } catch (e: Exception) {
            Logger.e("Error initializing Firebase [$e]")
        }
    }
}