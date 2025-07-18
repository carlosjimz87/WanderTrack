package com.carlosjimz87.wandertrack.utils

object Logger {

    private const val GLOBAL_TAG = "WanderTrack::"

    fun d(message: String) {
        println("Logger.d -> $GLOBAL_TAG $message")
    }

    fun i(message: String) {
        println("Logger.i -> $GLOBAL_TAG $message")
    }

    fun e(message: String, throwable: Throwable? = null) {
        println("Logger.e -> $GLOBAL_TAG ${throwable?.message ?: message}")
    }

    fun w(message: String) {
        println("Logger.w -> $GLOBAL_TAG $message")
    }
}
