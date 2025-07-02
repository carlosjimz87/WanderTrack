package com.carlosjimz87.wandertrack.data.repo

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit)
    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()
}