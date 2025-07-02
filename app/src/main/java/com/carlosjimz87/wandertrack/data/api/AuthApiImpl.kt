package com.carlosjimz87.wandertrack.data.api

import androidx.lifecycle.ViewModel
import com.carlosjimz87.wandertrack.domain.models.AuthData
import com.carlosjimz87.wandertrack.domain.models.AuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val api: AuthApi) : ViewModel() {

    private val _authState = MutableStateFlow<FirebaseUser?>(null)
    val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        api.login(AuthProvider.EMAIL, AuthData.Email(email, password)) { success, msg ->
            if (success) _authState.value = FirebaseAuth.getInstance().currentUser
            onResult(success, msg)
        }
    }

    fun loginWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        api.login(AuthProvider.GOOGLE, AuthData.Google(idToken)) { success, msg ->
            if (success) _authState.value = FirebaseAuth.getInstance().currentUser
            onResult(success, msg)
        }
    }

    fun logout() {
        api.logout()
        _authState.value = null
    }
}