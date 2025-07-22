package com.carlosjimz87.wandertrack.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val oneTapClient = remember { Identity.getSignInClient(context) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                onGoogleIdTokenReceived(idToken)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.google_sign_failed),
                Toast.LENGTH_SHORT
            ).show()
            Logger.e("LoginScreen: Google sign in failed [${e.localizedMessage}]")
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        error = error,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onSignInClick = {
            authViewModel.loginWithEmail(email, password) { success, msg ->
                if (!success) error = msg
            }
        },
        onGoogleSignInClick = {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    launcher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                }
                .addOnFailureListener { err ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.google_sign_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    Logger.e("LoginScreen: Google sign in failed [${err.localizedMessage}]")
                }
        },
        onForgotPasswordClick = {
            // Handle forgot password navigation or logic here
        },
        resendVerificationEmail = {
            authViewModel.resendVerificationEmail { success, msg ->
                Toast.makeText(context, msg ?: "", Toast.LENGTH_SHORT).show()
            }
        }
    )
}