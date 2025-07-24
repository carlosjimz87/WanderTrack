package com.carlosjimz87.wandertrack.domain.repo

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun isUserLoggedIn(): Boolean

    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit)
    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()
    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit)
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit)
}