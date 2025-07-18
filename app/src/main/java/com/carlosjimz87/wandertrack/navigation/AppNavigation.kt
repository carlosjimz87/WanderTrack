package com.carlosjimz87.wandertrack.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.profile.ProfileScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import com.carlosjimz87.wandertrack.utils.Logger
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = Screens.SPLASH.name) {

        composable(Screens.SPLASH.name) {
            Logger.d( "Navigated to: SPLASH")
            val user = authViewModel.authState.collectAsState().value
            SplashScreen(
                onSplashFinished = {
                    if (user != null) {
                        Logger.d( "User authenticated. Navigating to: MAP")
                        navController.navigate(Screens.MAP.name) {
                            popUpTo(Screens.SPLASH.name) { inclusive = true }
                        }
                    } else {
                        Logger.d( "User not authenticated. Navigating to: AUTH")
                        navController.navigate(Screens.AUTH.name) {
                            popUpTo(Screens.SPLASH.name) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screens.AUTH.name) {
            Logger.d( "Navigated to: AUTH")
            AuthScreen(
                onGetStartedClick = {
                    Logger.d( "Navigating to: SIGNUP")
                    navController.navigate(Screens.SIGNUP.name)
                },
                onSignInClick = {
                    Logger.d( "Navigating to: LOGIN")
                    navController.navigate(Screens.LOGIN.name)
                }
            )
        }

        composable(Screens.LOGIN.name) {
            Logger.d( "Navigated to: LOGIN")
            LoginScreen(
                navController = navController,
                onGoogleIdTokenReceived = { idToken ->
                    Logger.d( "Google ID Token received. Attempting login.")
                    authViewModel.loginWithGoogle(idToken) { success, msg ->
                        if (!success) {
                            Logger.d( "Google login failed: $msg")
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                        } else {
                            Logger.d( "Google login successful")
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screens.SIGNUP.name) {
            Logger.d( "Navigated to: SIGNUP")
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screens.MAP.name) {
            Logger.d( "Navigated to: MAP")
            val authViewModel: AuthViewModel = koinViewModel()
            val user = authViewModel.authState.collectAsState().value

            user?.uid?.let { userId ->
                MapScreen(
                    userId = userId,
                    onProfileClick = {
                        Logger.d( "Navigating to: PROFILE")
                        navController.navigate(Screens.PROFILE.name)
                    }
                )
            } ?: Logger.d( "User not found for MAP screen")
        }

        composable(Screens.PROFILE.name) {
            Logger.d( "Navigated to: PROFILE")
            ProfileScreen(navController = navController)
        }
    }
}