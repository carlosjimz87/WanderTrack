package com.carlosjimz87.wandertrack.ui.screens.auth

import android.widget.Toast
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
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.authUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                (uiState as AuthUiState.Success).message?.let {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                }
                authViewModel.clearAuthUiState()
                onNavigateToLogin()
            }

            is AuthUiState.Error -> {
                val msg = (uiState as AuthUiState.Error).message
                if (msg.length > 80) {
                    dialogMessage = msg
                    showDialog = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
                authViewModel.clearAuthUiState()
            }

            else -> Unit
        }
    }

    if (showDialog) {
        MessageDialog(message = dialogMessage) { showDialog = false }
    }

    SignUpScreenContent(
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        isLoading = uiState is AuthUiState.Loading,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onGetStartedClick = {
            if (password != confirmPassword) {
                dialogMessage = context.getString(R.string.passwords_do_not_match)
                showDialog = true
            } else {
                authViewModel.signup(email.trim(), password.trim()) { _, _ -> }
            }
        },
        onSignInClick = onNavigateToLogin,
        snackbarHostState = snackbarHostState
    )
}