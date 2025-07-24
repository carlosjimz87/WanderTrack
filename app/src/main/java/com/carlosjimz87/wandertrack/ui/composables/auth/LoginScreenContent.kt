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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    resendVerificationEmail: () -> Unit,
    showResendButton: Boolean = false,
    isLoading: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
    val isFormValid = isEmailValid && password.trim().isNotEmpty()
    var attemptedSignIn by remember { mutableStateOf(false) }

    val emailSanitized = remember(email) { email.replace(Regex("[<>\"']"), "") }
    val passwordSanitized = remember(password) { password.replace(Regex("[<>\"']"), "") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val titleFontSize = calculateResponsiveFontSize(screenWidth)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoogleButton(
                    onGoogleSignInClick = onGoogleSignInClick,
                    enabled = !isLoading,
                    modifier = Modifier.semantics {
                        contentDescription = "Sign in with Google"
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = emailSanitized,
                    onValueChange = { onEmailChange(it.replace(Regex("[<>\"']"), "")) },
                    label = { Text(stringResource(R.string.email_address)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocusRequester),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    isError = email.isNotBlank() && !isEmailValid,
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                if (!isEmailValid && email.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.invalid_email),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = passwordSanitized,
                    onValueChange = { onPasswordChange(it.replace(Regex("[<>\"']"), "")) },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isFormValid) onSignInClick()
                        }
                    ),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val desc = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = desc)
                        }
                    },
                    isError = attemptedSignIn && password.isBlank(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors()
                )

                if (attemptedSignIn && password.isBlank()) {
                    Text(
                        text = stringResource(R.string.please_enter_your_password),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.semantics {
                        contentDescription = "Forgot password"
                    }
                ) {
                    Text(
                        stringResource(R.string.forgot_password),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showResendButton) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = resendVerificationEmail) {
                        Text(
                            text = stringResource(R.string.resend_verification_email),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = stringResource(R.string.sign_in),
                    onClick = {
                        attemptedSignIn = true
                        if (isFormValid) onSignInClick()
                    },
                    enabled = !isLoading,
                    modifier = Modifier.semantics {
                        contentDescription = "Sign in with email and password"
                    }
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
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            onGoogleSignInClick = {},
            onForgotPasswordClick = {},
            resendVerificationEmail = {}
        )
    }
}