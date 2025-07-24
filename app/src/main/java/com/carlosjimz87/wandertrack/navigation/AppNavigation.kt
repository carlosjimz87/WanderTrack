package com.carlosjimz87.wandertrack.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.common.isLoggedIn
import com.carlosjimz87.wandertrack.domain.models.Screens
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.profile.ProfileScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val validSession by authViewModel.validSession.collectAsState()
    var hasFinishedSplash by remember { mutableStateOf(false) }

    val controller = rememberMyNavController(
        initial = when (validSession) {
            true -> Screens.Map
            else -> Screens.Splash
        }
    )
    val currentScreen = controller.current

    LaunchedEffect(hasFinishedSplash, validSession) {
        if (!hasFinishedSplash) return@LaunchedEffect
        controller.replace(if (validSession.isLoggedIn()) Screens.Map else Screens.Auth)
    }

    LaunchedEffect(currentScreen, validSession) {
        if (!validSession.isLoggedIn() && currentScreen.isProtected()) {
            controller.replace(Screens.Auth)
        }
    }

    LaunchedEffect(validSession, currentScreen) {
        if (validSession.isLoggedIn() && currentScreen.isPublic()) {
            controller.replace(Screens.Map)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState.order > initialState.order) {
                slideInHorizontally { it } + fadeIn(tween(500)) togetherWith
                        slideOutHorizontally { -it } + fadeOut(tween(500))
            } else {
                slideInHorizontally { -it } + fadeIn(tween(500)) togetherWith
                        slideOutHorizontally { it } + fadeOut(tween(500))
            }
        }
    ) { screen ->
        when (screen) {
            is Screens.Splash -> SplashScreen(
                onSplashFinished = {
                    hasFinishedSplash = true
                }
            )

            is Screens.Auth -> AuthScreen(
                onGetStartedClick = { controller.navigate(Screens.SignUp) },
                onSignInClick = { controller.navigate(Screens.Login) }
            )

            is Screens.Login -> LoginScreen(
                onGoogleIdTokenReceived = { idToken ->
                    authViewModel.loginWithGoogle(idToken) { success, _ ->
                        if (!success) {
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                onBack = { controller.replace(Screens.Auth) }
            )

            is Screens.SignUp -> SignUpScreen(
                onNavigateToLogin = {
                    controller.replace(Screens.Login)
                    Toast.makeText(
                        context,
                        "Account created. Please verify your email before logging in.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onBack = { controller.replace(Screens.Auth) }
            )

            is Screens.Map -> {
                val userId = authViewModel.userId
                userId?.let {
                    MapScreen(
                        userId = it,
                        onProfileClick = { controller.navigate(Screens.Profile) }
                    )
                } ?: run {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    controller.replace(Screens.Auth)
                }
            }

            is Screens.Profile -> ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    controller.replace(Screens.Auth)
                },
                onBack = {
                    controller.replace(if (validSession == true) Screens.Map else Screens.Auth)
                }
            )
        }
    }
}