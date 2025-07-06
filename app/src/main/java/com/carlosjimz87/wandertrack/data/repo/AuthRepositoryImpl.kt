package com.carlosjimz87.wandertrack.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    companion object{
        const val VERIFY_EMAIL_FIRST = "Please verify your email address before signing in."
    }

    override fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null && user.isEmailVerified) {
                    onResult(true, null)
                } else {
                    auth.signOut()
                    onResult(false, VERIFY_EMAIL_FIRST)
                }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    override fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.sendEmailVerification()
                onResult(true, null)
            }
            .addOnFailureListener { onResult(false, it.message) }
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
}