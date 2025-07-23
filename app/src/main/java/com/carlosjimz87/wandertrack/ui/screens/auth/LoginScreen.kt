package com.carlosjimz87.wandertrack.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.composables.auth.LoginScreenContent
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.utils.Logger
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onGoogleIdTokenReceived: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    context.SetBottomBarColor()

    val emailState = rememberSaveable { mutableStateOf("") }
    val passwordState = rememberSaveable { mutableStateOf("") }
    val errorState = rememberSaveable { mutableStateOf<String?>(null) }

    val oneTapClient = remember { Identity.getSignInClient(context) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        runCatching {
            oneTapClient.getSignInCredentialFromIntent(result.data).googleIdToken
        }.onSuccess { token ->
            token?.let { onGoogleIdTokenReceived(it) }
        }.onFailure {
            Toast.makeText(context, R.string.google_sign_failed, Toast.LENGTH_SHORT).show()
            Logger.e("Google sign in failed: ${it.localizedMessage}")
        }
    }

    LoginScreenContent(
        email = emailState.value,
        password = passwordState.value,
        error = errorState.value,
        onEmailChange = { emailState.value = it },
        onPasswordChange = { passwordState.value = it },
        onSignInClick = {
            authViewModel.loginWithEmail(emailState.value, passwordState.value) { success, msg ->
                if (!success) errorState.value = msg
            }
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
                .addOnSuccessListener { result ->
                    googleLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent).build())
                }
                .addOnFailureListener {
                    Toast.makeText(context, R.string.google_sign_failed, Toast.LENGTH_SHORT).show()
                    Logger.e("Google sign in failed: ${it.localizedMessage}")
                }
        },
        onForgotPasswordClick = {
            // TODO: Implement forgot password
        },
        resendVerificationEmail = {
            authViewModel.resendVerificationEmail { _, msg ->
                Toast.makeText(context, msg ?: "", Toast.LENGTH_SHORT).show()
            }
        }
    )
}