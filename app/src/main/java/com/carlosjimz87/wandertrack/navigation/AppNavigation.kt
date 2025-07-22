package com.carlosjimz87.wandertrack.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.domain.models.Screens
import com.carlosjimz87.wandertrack.managers.StoreManager
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.profile.ProfileScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = koinViewModel(),
    dataStoreManager: StoreManager = koinInject()
) {
    val context = LocalContext.current
    val firebaseUser = authViewModel.authState.collectAsState().value
    val controller = rememberMyNavController(initial = Screens.Splash)
    val currentScreen = controller.current

    var restored by remember { mutableStateOf(false) }

    // Restore last screen if user is logged in
    LaunchedEffect(firebaseUser) {
        if (!restored) {
            dataStoreManager.lastScreen.collect { last ->
                restored = true
                if (firebaseUser != null) {
                    val restoredScreen = Screens.fromRouteString(last, firebaseUser.uid)
                    controller.replace(restoredScreen)
                } else {
                    controller.replace(Screens.Splash)
                }
            }
        }
    }

    // Save screen on each navigation
    LaunchedEffect(currentScreen) {
        if (currentScreen !is Screens.Splash) {
            dataStoreManager.saveLastScreen(currentScreen.toRouteString())
        }
    }

    // Redirect to Auth if user is logged out
    LaunchedEffect(firebaseUser) {
        if ((currentScreen is Screens.Map || currentScreen is Screens.Profile) && firebaseUser == null) {
            controller.replace(Screens.Auth)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState.order > initialState.order) {
                // Forward
                slideInHorizontally { it } + fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                        slideOutHorizontally { -it } + fadeOut(tween(500, easing = FastOutSlowInEasing))
            } else {
                // Back
                slideInHorizontally { -it } + fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
                        slideOutHorizontally { it } + fadeOut(tween(500, easing = FastOutSlowInEasing))
            }
        }
    ) { screen ->
        when (screen) {
            is Screens.Splash -> SplashScreen(
                onSplashFinished = {
                    if (firebaseUser != null) {
                        controller.replace(Screens.Map(firebaseUser.uid))
                    } else {
                        controller.replace(Screens.Auth)
                    }
                }
            )

            is Screens.Auth -> AuthScreen(
                onGetStartedClick = { controller.navigate(Screens.SignUp) },
                onSignInClick = { controller.navigate(Screens.Login) }
            )

            is Screens.Login -> {
                LaunchedEffect(firebaseUser) {
                    if (firebaseUser != null && firebaseUser.isEmailVerified) {
                        controller.replace(Screens.Map(firebaseUser.uid))
                    }
                }

                LoginScreen(
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
            }

            is Screens.SignUp -> {
                LaunchedEffect(firebaseUser) {
                    if (firebaseUser != null) {
                        controller.replace(Screens.Map(firebaseUser.uid))
                    }
                }

                SignUpScreen(
                    onNavigateToLogin = { controller.replace(Screens.Login) },
                    onBack = { controller.replace(Screens.Auth) }
                )
            }

            is Screens.Map -> MapScreen(
                userId = screen.userId,
                onProfileClick = { controller.navigate(Screens.Profile) }
            )

            is Screens.Profile -> ProfileScreen(
                onLogout = { controller.replace(Screens.Auth) },
                onBack = {
                    if (firebaseUser != null) {
                        controller.replace(Screens.Map(firebaseUser.uid, Screens.Map.Source.BackFromProfile))
                    } else {
                        controller.replace(Screens.Auth)
                    }
                }
            )
        }
    }
}