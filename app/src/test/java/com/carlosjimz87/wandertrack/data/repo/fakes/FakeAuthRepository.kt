package com.carlosjimz87.wandertrack.data.repo.fakes

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk

class FakeAuthRepository : AuthRepository {

    private var _fakeUser: FirebaseUser? = null
    var shouldFail = false
    var shouldResetPasswordFail = false
    private var googleLoginSuccess = true
    var isEmailVerified = true
    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastGoogleIdToken: String? = null
    var lastResetEmail: String? = null
    var logoutCalled = false
    var resendVerificationCalled = false
    var resendVerificationShouldFail = false

    override val currentUser: FirebaseUser?
        get() = _fakeUser

    override fun isUserLoggedIn(): Boolean {
        return _fakeUser?.isEmailVerified == true
    }

    override fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        lastEmail = email
        lastPassword = password

        if (shouldFail) {
            _fakeUser = null
            onResult(false, "Login failed")
        } else {
            _fakeUser = mockFirebaseUser("testUserId", email)
            if (!_fakeUser!!.isEmailVerified) {
                onResult(false, "Please verify your email before continuing.")
            } else {
                onResult(true, null)
            }
        }
    }

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (shouldFail) {
            _fakeUser = null
            onResult(false, "Signup failed")
        } else {
            _fakeUser = mockFirebaseUser("testUserId", email)
            onResult(true, "Registration successful. We've sent you a verification email. Please check your inbox — and your spam folder just in case!")
        }
    }

    override fun resendVerificationEmail(onResult: (Boolean, String?) -> Unit) {
        resendVerificationCalled = true
        val user = _fakeUser

        if (resendVerificationShouldFail) {
            onResult(false, "Failed to send verification email.")
        } else if (user != null) {
            onResult(true, "Verification email sent.")
        } else {
            onResult(false, "User not found.")
        }
    }

    override fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        lastResetEmail = email

        if (shouldResetPasswordFail) {
            onResult(false, "Failed to send password reset email.")
        } else {
            onResult(true, "Password reset email sent.")
        }
    }

    override fun logout() {
        _fakeUser = null
        logoutCalled = true
    }

    fun setGoogleLoginResult(success: Boolean) {
        googleLoginSuccess = success
    }

    override fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        lastGoogleIdToken = idToken

        if (googleLoginSuccess) {
            _fakeUser = mockFirebaseUser("testUserId", "testuser@gmail.com")
            onResult(true, null)
        } else {
            _fakeUser = null
            onResult(false, "Google login failed")
        }
    }

    private fun mockFirebaseUser(uid: String, email: String): FirebaseUser {
        val user = mockk<FirebaseUser>(relaxed = true)
        every { user.uid } returns uid
        every { user.email } returns email
        every { user.isEmailVerified } returns isEmailVerified
        return user
    }
}