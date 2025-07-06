package com.carlosjimz87.wandertrack.ui.composables.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.data.repo.AuthRepositoryImpl
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun LoginScreenContent(
    email: String,
    password: String,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    resendVerificationEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val isFormValid = isEmailValid && password.isNotBlank()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp)
    ) {
        val screenHeight = this@BoxWithConstraints.maxHeight
        val screenWidth = this@BoxWithConstraints.maxWidth
        val titleFontSize = calculateResponsiveFontSize(screenWidth)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = stringResource(R.string.sign_in_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoogleButton(onGoogleSignInClick = onGoogleSignInClick)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.email_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = email.isNotBlank() && !isEmailValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        stringResource(R.string.forgot_password),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (error == AuthRepositoryImpl.VERIFY_EMAIL_FIRST) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.resend_verification_email),
                        onClick = { resendVerificationEmail() }
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.sign_in),
                    onClick = onSignInClick,
                    enabled = isFormValid
                )
            }
        }
    }
}

@Preview(
    name = "LoginScreen Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun LoginScreenContentPreviewLight() {
    WanderTrackTheme {
        LoginScreenContent(
            email = "user@email.com",
            password = "password",
            error = null,
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            onGoogleSignInClick = {},
            onForgotPasswordClick = {},
            resendVerificationEmail = {}
        )
    }
}

@Preview(
    name = "LoginScreen Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenContentPreviewDark() {
    WanderTrackTheme {
        LoginScreenContent(
            email = "user@email.com",
            password = "password",
            error = null,
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            onGoogleSignInClick = {},
            onForgotPasswordClick = {},
            resendVerificationEmail = {}
        )
    }
}