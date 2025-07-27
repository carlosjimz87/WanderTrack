package com.carlosjimz87.wandertrack.data.repo

import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.common.AppConfig
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


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
        return user != null && (user.isEmailVerified || AppConfig.isDev)
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null && (!user.isEmailVerified && BuildConfig.FIREBASE_ENV != "dev")) {
                        cont.resume(Result.failure(Exception("Please verify your email before continuing.")))
                    } else {
                        cont.resume(Result.success(Unit))
                    }
                }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { cont.resume(Result.failure(it)) }
        }

    override suspend fun signup(email: String, password: String): Result<String> =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user == null) {
                            cont.resume(Result.failure(Exception("Unexpected error: user not found after registration.")))
                        } else {
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    cont.resume(
                                        Result.success(
                                            "Registration successful. We've sent you a verification email. Please check your inbox — and your spam folder just in case!"
                                        )
                                    )
                                }
                                .addOnFailureListener {
                                    cont.resume(Result.failure(Exception("Failed to send verification email: ${it.message}")))
                                }
                        }
                    } else {
                        cont.resume(Result.failure(Exception(task.exception?.localizedMessage ?: "Unknown signup error")))
                    }
                }
        }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun resendVerificationEmail(): Result<String> =
        suspendCancellableCoroutine { cont ->
            val user = auth.currentUser
            if (user != null) {
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        cont.resume(Result.success("Verification email sent."))
                    }
                    .addOnFailureListener {
                        cont.resume(Result.failure(Exception("Failed to send verification email.")))
                    }
            } else {
                cont.resume(Result.failure(Exception("User not found.")))
            }
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(it.exception ?: Exception("Failed to send password reset email.")))
                    }
                }
        }
}