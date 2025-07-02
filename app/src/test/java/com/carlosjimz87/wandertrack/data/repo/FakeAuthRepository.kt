package com.carlosjimz87.wandertrack.data.repo

import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito

class FakeAuthRepository : AuthRepository {

    private var fakeUser: FirebaseUser? = null
    var shouldFail = false
    private var googleLoginSuccess = true

    override val currentUser: FirebaseUser?
        get() = fakeUser

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (shouldFail) {
            onResult(false, "Login failed")
        } else {
            fakeUser = Mockito.mock(FirebaseUser::class.java)
            onResult(true, null)
        }
    }

    override fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        login(email, password, onResult)
    }

    override fun logout() {
        fakeUser = null
    }

    fun setGoogleLoginResult(success: Boolean) {
        googleLoginSuccess = success
    }

    override fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        if (googleLoginSuccess) {
            fakeUser = Mockito.mock(FirebaseUser::class.java)
            onResult(true, null)
        } else {
            onResult(false, "Error")
        }
    }
}