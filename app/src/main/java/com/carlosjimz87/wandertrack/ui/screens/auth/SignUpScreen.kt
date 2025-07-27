package com.carlosjimz87.wandertrack.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.auth.MessageDialog
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
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    // 🔹 Handle success navigation
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onNavigateToLogin()
            authViewModel.clearUiState()
        }
    }

    // 🔹 Show snackbar or dialog based on message length
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearUiState()
        }

        uiState.errorMessage?.let { msg ->
            if (msg.length > 80) {
                dialogMessage = msg
                showDialog = true
            } else {
                snackbarHostState.showSnackbar(msg)
            }
            authViewModel.clearUiState()
        }
    }

    if (showDialog) {
        MessageDialog(message = dialogMessage) { showDialog = false }
    }

    SignUpScreenContent(
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        isLoading = uiState.isLoading,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onGetStartedClick = {
            if (password != confirmPassword) {
                dialogMessage = context.getString(R.string.passwords_do_not_match)
                showDialog = true
            } else {
                authViewModel.signup(email.trim(), password.trim())
            }
        },
        onSignInClick = onNavigateToLogin,
        snackbarHostState = snackbarHostState
    )
}