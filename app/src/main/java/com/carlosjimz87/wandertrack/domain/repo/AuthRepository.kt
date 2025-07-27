package com.carlosjimz87.wandertrack.domain.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


interface AuthRepository {
    val currentUser: FirebaseUser?
    fun isUserLoggedIn(): Boolean

    suspend fun loginWithEmail(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun signup(email: String, password: String): Result<String>
    fun logout()
    suspend fun resendVerificationEmail(): Result<String>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener)
}