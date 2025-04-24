package com.carlosjimz87.wandertrack.utils

import android.util.Log

object Logger {

    private const val GLOBAL_TAG = "WanderTrack::"

    fun d(message: String) {
        Log.d(GLOBAL_TAG, message)
    }

    fun i(message: String) {
        Log.i(GLOBAL_TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(GLOBAL_TAG, throwable?.message ?: message)
    }

    fun w(message: String) {
        Log.w(GLOBAL_TAG, message)
    }
}
