package com.carlosjimz87.wandertrack.ui.composables.auth

import android.content.res.Configuration
import android.util.Patterns
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun SignUpScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean = false,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val focusManager = LocalFocusManager.current
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val confirmVisible = rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
    val isPasswordValid = password.trim().length >= 6
    val isFormValid = isEmailValid && isPasswordValid && password == confirmPassword
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmFocusRequester = remember { FocusRequester() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = stringResource(R.string.create_account),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.create_account_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.email_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    isError = email.isNotBlank() && !isEmailValid,
                    enabled = !isLoading,
                    singleLine = true
                )
                if (!isEmailValid && email.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.invalid_email),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(icon, contentDescription = "Toggle password")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmFocusRequester.requestFocus() }
                    ),
                    isError = password.isNotBlank() && !isPasswordValid,
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.confirm_password)) },
                    modifier = Modifier.fillMaxWidth().focusRequester(confirmFocusRequester),
                    visualTransformation = if (confirmVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (confirmVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { confirmVisible.value = !confirmVisible.value }) {
                            Icon(icon, contentDescription = "Toggle confirm password")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            onGetStartedClick()
                        }
                    ),
                    isError = confirmPassword.isNotBlank() && password != confirmPassword,
                    enabled = !isLoading,
                    singleLine = true
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = stringResource(R.string.sign_up),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onGetStartedClick()
                    },
                    enabled = isFormValid && !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onSignInClick) {
                    Text(stringResource(R.string.already_have_account), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(
    name = "SignUpScreen Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SignUpScreenContentPreviewLight() {
    WanderTrackTheme {
        SignUpScreenContent(
            email = "test@email.com",
            password = "password",
            confirmPassword = "password",
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onGetStartedClick = {},
            onSignInClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(
    name = "SignUpScreen Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SignUpScreenContentPreviewDark() {
    WanderTrackTheme {
        SignUpScreenContent(
            email = "test@email.com",
            password = "password",
            confirmPassword = "password",
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onGetStartedClick = {},
            onSignInClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}