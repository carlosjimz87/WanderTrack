package com.carlosjimz87.wandertrack.domain.repo

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit)
    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit)
    fun logout()
}