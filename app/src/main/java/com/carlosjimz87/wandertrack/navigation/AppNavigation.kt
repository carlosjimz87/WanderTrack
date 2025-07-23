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
import androidx.compose.ui.platform.LocalContext
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
    val firebaseUser = authViewModel.authState.collectAsState().value
    val controller = rememberMyNavController(initial = Screens.Splash)
    val currentScreen = controller.current

    // Redirige desde pantallas protegidas si el usuario está deslogueado
    LaunchedEffect(firebaseUser, currentScreen) {
        if ((currentScreen is Screens.Map || currentScreen is Screens.Profile) && firebaseUser == null) {
            controller.replace(Screens.Auth)
        }
    }

    // Redirige desde pantallas públicas si el usuario se loguea
    LaunchedEffect(firebaseUser) {
        if ((currentScreen is Screens.Login || currentScreen is Screens.SignUp || currentScreen is Screens.Auth)
            && firebaseUser != null
        ) {
            controller.replace(Screens.Map(firebaseUser.uid))
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

            is Screens.Login -> LoginScreen(
                onGoogleIdTokenReceived = { idToken ->
                    authViewModel.loginWithGoogle(idToken) { success, _ ->
                        if (!success) {
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBack = { controller.replace(Screens.Auth) }
            )

            is Screens.SignUp -> SignUpScreen(
                onNavigateToLogin = { controller.replace(Screens.Login) },
                onBack = { controller.replace(Screens.Auth) }
            )

            is Screens.Map -> MapScreen(
                userId = screen.userId,
                onProfileClick = { controller.navigate(Screens.Profile) }
            )

            is Screens.Profile -> ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    controller.replace(Screens.Auth)
                },
                onBack = {
                    controller.replace(
                        firebaseUser?.let { Screens.Map(it.uid, Screens.Map.Source.BackFromProfile) }
                            ?: Screens.Auth
                    )
                }
            )
        }
    }
}