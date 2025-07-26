package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {


    override fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        return user != null && (user.isEmailVerified || BuildConfig.FIREBASE_ENV == "dev")
    }

    override fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && (!user.isEmailVerified && BuildConfig.FIREBASE_ENV != "dev")) {
                    onResult(false, "Please verify your email before continuing.")
                } else {
                    onResult(true, null)
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    override fun loginWithGoogle(
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    override fun signup(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user == null) {
                        onResult(false, "Unexpected error: user not found after registration.")
                        return@addOnCompleteListener
                    }

                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            onResult(
                                true,
                                "Registration successful. We've sent you a verification email. Please check your inbox — and your spam folder just in case!"
                            )
                        }
                        .addOnFailureListener { error ->
                            onResult(false, "Failed to send verification email: ${error.message}")
                        }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Unknown signup error"
                    onResult(false, errorMsg)
                }
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnSuccessListener { onResult(true, "Verification email sent.") }
                .addOnFailureListener { onResult(false, "Failed to send verification email.") }
        } else {
            onResult(false, "User not found.")
        }
    }

    override fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                onResult(it.isSuccessful, if (it.isSuccessful) null else it.exception?.message)
            }
    }
}