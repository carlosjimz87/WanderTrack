package com.carlosjimz87.wandertrack.data.repo.fakes

import com.carlosjimz87.wandertrack.domain.repo.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk

class FakeAuthRepository : AuthRepository {

    private var _fakeUser: FirebaseUser? = null
    var shouldFail = false
    private var googleLoginSuccess = true

    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastGoogleIdToken: String? = null
    var logoutCalled = false

    override val currentUser: FirebaseUser?
        get() = _fakeUser

    override fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        lastEmail = email
        lastPassword = password

        if (shouldFail) {
            _fakeUser = null
            onResult(false, "Login failed")
        } else {
            _fakeUser = mockFirebaseUser("testUserId", email)
            onResult(true, null)
        }
    }

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        loginWithEmail(email, password, onResult)
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
        val user = mockk<FirebaseUser>()
        every { user.uid } returns uid
        every { user.email } returns email
        return user
    }
}