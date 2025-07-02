package com.carlosjimz87.wandertrack.ui.screens.auth
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carlosjimz87.wandertrack.BuildConfig
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.navigation.Screens
import com.carlosjimz87.wandertrack.ui.composables.auth.GoogleButton
import com.carlosjimz87.wandertrack.ui.composables.auth.LoginButtons
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel(),
    onGoogleIdTokenReceived: (String) -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.authState.collectAsState()

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
            Toast.makeText(context, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screens.MAP.name) {
                popUpTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = context.getString(R.string.welcome_back),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = context.getString(R.string.sign_in_into_your_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoogleButton(onGoogleSignInClick = {

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
                        .addOnFailureListener {
                            Toast.makeText(context, "Google Sign-In init failed", Toast.LENGTH_SHORT).show()
                        }
                } )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(context.getString(R.string.email_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(context.getString(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    // go to webview to handle forgotten password.
                }) {
                    Text(context.getString(R.string.forgot_password), color = MaterialTheme.colorScheme.primary)
                }
            }

            LoginButtons(
                onSignInClick = {
                    authViewModel.loginWithEmail(email, password) { success, msg ->
                        if (!success) error = msg
                    }
                }
            )
        }
    }
}