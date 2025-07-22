package com.carlosjimz87.wandertrack.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.auth.SignUpScreenContent
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }


    SignUpScreenContent(
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        error = error,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onGetStartedClick = {
            if (password != confirmPassword) {
                error = context.getString(R.string.passwords_do_not_match)
            } else {
                authViewModel.signup(email, password) { success, msg ->
                    error = msg
                    if (success) {
                        Toast.makeText(context, msg ?: "", Toast.LENGTH_LONG).show()
                    }
                }
            }
        },
        onSignInClick = onNavigateToLogin
    )
}