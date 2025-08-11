package com.carlosjimz87.wandertrack.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.composables.auth.LoginScreenContent
import com.carlosjimz87.wandertrack.ui.composables.auth.MessageDialog
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onGoogleIdTokenReceived: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    context.SetBottomBarColor()

    val emailState = rememberSaveable { mutableStateOf("") }
    val passwordState = rememberSaveable { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 🔹 Collect Auth UI State
    val uiState by authViewModel.authUiState.collectAsState()

    // 🔹 Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 🔹 Message handling
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearUiState()
        }

        uiState.errorMessage?.let { msg ->
            if (msg.length > 80 || uiState.showResendButton) {
                dialogMessage = msg
                showDialog = true
            } else {
                snackbarHostState.showSnackbar(msg)
            }
            authViewModel.clearUiState()
        }
    }
    LaunchedEffect(uiState.verificationEmailSent) {

        val verificationEmailMsg = if (uiState.verificationEmailSent) uiState.successMessage
            else uiState.errorMessage
        verificationEmailMsg?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    val oneTapClient = remember { Identity.getSignInClient(context) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        runCatching {
            oneTapClient.getSignInCredentialFromIntent(result.data).googleIdToken
        }.onSuccess { token ->
            token?.let { onGoogleIdTokenReceived(it) }
        }.onFailure {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.google_sign_failed))
            }
            Logger.e("Google sign in failed: ${it.localizedMessage}")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->

        LoginScreenContent(
            email = emailState.value,
            password = passwordState.value,
            onEmailChange = { emailState.value = it },
            onPasswordChange = { passwordState.value = it },
            onSignInClick = {
                authViewModel.loginWithEmail(emailState.value, passwordState.value)
            },
            onGoogleSignInClick = {
                val request = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    ).build()

                oneTapClient.beginSignIn(request)
                    .addOnSuccessListener {
                        googleLauncher.launch(IntentSenderRequest.Builder(it.pendingIntent).build())
                    }
                    .addOnFailureListener {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.google_sign_failed))
                        }
                        Logger.e("Google sign in failed: ${it.localizedMessage}")
                    }
            },
            onForgotPasswordClick = {
                // TODO: Implement forgot password
            },
            resendVerificationEmail = {
                authViewModel.resendVerificationEmail()
            },
            showResendButton = uiState.showResendButton,
            isLoading = uiState.isLoading,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showDialog) {
        MessageDialog(message = dialogMessage) { showDialog = false }
    }
}