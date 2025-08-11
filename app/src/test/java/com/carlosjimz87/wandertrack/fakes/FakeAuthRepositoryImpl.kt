package com.carlosjimz87.wandertrack.fakes

import android.net.Uri
import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk

class FakeAuthRepositoryImpl : AuthRepository {

    // ====== Tipos para controlar resultados desde los tests ======
    sealed class Outcome {
        data object Success : Outcome()
        data class Error(val code: String, val message: String = code) : Outcome()
        data object EmailNotVerified : Outcome() // específico para login email
    }

    class AuthError(msg: String) : Exception(msg)

    // Por operación (puedes cambiarlos en cada test)
    var nextEmailLogin: Outcome = Outcome.Success
    var nextGoogleLogin: Outcome = Outcome.Success
    var nextSignup: Outcome = Outcome.Success
    var nextResendVerification: Outcome = Outcome.Success
    var nextPasswordReset: Outcome = Outcome.Success

    // Estado del fake
    var isEmailVerified: Boolean = true
    private var _fakeUser: FirebaseUser? = null
    override val currentUser: FirebaseUser? get() = _fakeUser

    // Trazas útiles para asserts
    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastGoogleIdToken: String? = null
    var lastResetEmail: String? = null
    var logoutCalled = false
    var resendVerificationCalled = false

    private val listeners = mutableListOf<FirebaseAuth.AuthStateListener>()

    // ====== Helpers para tests ======
    fun seedLoggedUser(uid: String = "uid", email: String = "user@test.com") {
        _fakeUser = mockFirebaseUser(uid, email)
        notifyAuthStateChanged()
    }

    fun reset() {
        _fakeUser = null
        isEmailVerified = true
        nextEmailLogin = Outcome.Success
        nextGoogleLogin = Outcome.Success
        nextSignup = Outcome.Success
        nextResendVerification = Outcome.Success
        nextPasswordReset = Outcome.Success
        lastEmail = null; lastPassword = null; lastGoogleIdToken = null; lastResetEmail = null
        logoutCalled = false; resendVerificationCalled = false
        listeners.clear()
    }

    // ====== API ======
    override fun isUserLoggedIn(): Boolean =
        _fakeUser != null && (_fakeUser?.isEmailVerified ?: false)

    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        lastEmail = email; lastPassword = password
        return when (val r = nextEmailLogin) {
            is Outcome.Success -> {
                _fakeUser = mockFirebaseUser("testUserId", email)
                if (!isEmailVerified) {
                    Result.failure(AuthError("EMAIL_NOT_VERIFIED"))
                } else {
                    notifyAuthStateChanged()
                    Result.success(Unit)
                }
            }

            is Outcome.EmailNotVerified -> {
                _fakeUser = mockFirebaseUser("testUserId", email)
                Result.failure(AuthError("EMAIL_NOT_VERIFIED"))
            }

            is Outcome.Error -> {
                _fakeUser = null
                Result.failure(AuthError(r.code))
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        lastGoogleIdToken = idToken
        return when (val r = nextGoogleLogin) {
            is Outcome.Success -> {
                _fakeUser = mockFirebaseUser("testUserId", "testuser@gmail.com")
                notifyAuthStateChanged()
                Result.success(Unit)
            }

            is Outcome.EmailNotVerified -> {
                _fakeUser = null
                Result.failure(AuthError("EMAIL_NOT_VERIFIED"))
            }

            is Outcome.Error -> {
                _fakeUser = null
                Result.failure(AuthError(r.code))
            }
        }
    }

    override suspend fun signup(email: String, password: String): Result<String> {
        return when (val r = nextSignup) {
            is Outcome.Success -> {
                _fakeUser = mockFirebaseUser("testUserId", email)
                // Mensaje esperado por la UI/tests
                Result.success("SIGNUP_OK_NEEDS_VERIFICATION")
            }

            is Outcome.Error -> {
                _fakeUser = null
                Result.failure(AuthError(r.code))
            }

            is Outcome.EmailNotVerified -> {
                _fakeUser = mockFirebaseUser("testUserId", email)
                Result.success("SIGNUP_OK_NEEDS_VERIFICATION")
            }
        }
    }

    override fun logout() {
        logoutCalled = true
        _fakeUser = null
        notifyAuthStateChanged()
    }

    override suspend fun resendVerificationEmail(): Result<String> {
        resendVerificationCalled = true
        val user = _fakeUser ?: return Result.failure(AuthError("USER_NOT_FOUND"))
        // Puedes hacer depender el éxito de nextResendVerification
        return when (val r = nextResendVerification) {
            is Outcome.Success, Outcome.EmailNotVerified -> Result.success("VERIFICATION_EMAIL_SENT")
            is Outcome.Error -> Result.failure(AuthError(r.code))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        lastResetEmail = email
        return when (val r = nextPasswordReset) {
            is Outcome.Success, Outcome.EmailNotVerified -> Result.success(Unit)
            is Outcome.Error -> Result.failure(AuthError(r.code))
        }
    }

    override fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        listeners += listener
    }

    // ====== Internos ======
    private fun notifyAuthStateChanged() {
        listeners.forEach { it.onAuthStateChanged(mockk(relaxed = true)) }
    }

    private fun mockFirebaseUser(uid: String, email: String): FirebaseUser {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { user.uid } returns uid
        every { user.email } returns email
        every { user.isEmailVerified } answers { this@FakeAuthRepositoryImpl.isEmailVerified }

        val fakeUri = mockk<Uri>(relaxed = true)
        every { fakeUri.toString() } returns "https://fake.com/avatar.png"
        every { user.photoUrl } returns fakeUri

        return user
    }
}