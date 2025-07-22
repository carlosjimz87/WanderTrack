package com.carlosjimz87.wandertrack.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.profile.ProfileScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import com.carlosjimz87.wandertrack.utils.Logger
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(authViewModel: AuthViewModel = koinViewModel()) {
    val controller = rememberMyNavController()
    val context = LocalContext.current
    val currentScreen = controller.backStack.last()
    val firebaseUser = authViewModel.authState.collectAsState().value

    LaunchedEffect(currentScreen) {
        Logger.d("Navigated to: $currentScreen")
    }

    LaunchedEffect(firebaseUser) {
        if ((currentScreen is Screen.Map || currentScreen is Screen.Profile) && firebaseUser == null) {
            controller.replace(Screen.Auth)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() with fadeOut() }
    ) { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen(
                onSplashFinished = {
                    if (firebaseUser != null) {
                        controller.replace(Screen.Map(firebaseUser.uid))
                    } else {
                        controller.replace(Screen.Auth)
                    }
                }
            )

            is Screen.Auth -> AuthScreen(
                onGetStartedClick = { controller.navigate(Screen.SignUp) },
                onSignInClick = { controller.navigate(Screen.Login) }
            )

            is Screen.Login -> {
                LaunchedEffect(firebaseUser) {
                    if (firebaseUser != null && firebaseUser.isEmailVerified) {
                        controller.replace(Screen.Map(firebaseUser.uid))
                    }
                }

                LoginScreen(
                    onGoogleIdTokenReceived = { idToken ->
                        authViewModel.loginWithGoogle(idToken) { success, msg ->
                            if (!success) {
                                Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onBack = { controller.replace(Screen.Auth) }
                )
            }

            is Screen.SignUp -> {
                LaunchedEffect(firebaseUser) {
                    if (firebaseUser != null) {
                        controller.replace(Screen.Map(firebaseUser.uid))
                    }
                }

                SignUpScreen(
                    onNavigateToLogin = { controller.replace(Screen.Login) },
                    onBack = { controller.replace(Screen.Auth) }
                )
            }

            is Screen.Map -> MapScreen(
                userId = screen.userId,
                onProfileClick = { controller.navigate(Screen.Profile) }
            )

            is Screen.Profile -> ProfileScreen(
                onLogout = { controller.replace(Screen.Auth) },
                onBack = { controller.pop() }
            )
        }
    }
}