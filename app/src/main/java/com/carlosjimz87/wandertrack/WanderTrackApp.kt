package com.carlosjimz87.wandertrack

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class WanderTrackApp : Application(){
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Log.i(TAG, "Firebase initialized successfully")
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Firebase has already been initialized", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }

    companion object {
        private const val TAG = "WanderTrackApp"
    }
}