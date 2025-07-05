package com.carlosjimz87.wandertrack.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel = koinViewModel()) {

    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = Screens.SPLASH.name) {

        composable(Screens.SPLASH.name) {
            val user = authViewModel.authState.collectAsState().value
            SplashScreen(
                onSplashFinished = {
                    if (user != null) {
                        navController.navigate(Screens.MAP.name) {
                            popUpTo(Screens.SPLASH.name) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screens.AUTH.name) {
                            popUpTo(Screens.SPLASH.name) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screens.AUTH.name) {
            AuthScreen(
                onGetStartedClick = { navController.navigate(Screens.SIGNUP.name) },
                onSignInClick = { navController.navigate(Screens.LOGIN.name) }
            )
        }

        composable(Screens.LOGIN.name) {
            LoginScreen(
                navController = navController,
                onGoogleIdTokenReceived = { idToken ->
                    authViewModel.loginWithGoogle(idToken) { success, msg ->
                        if (!success) {
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screens.SIGNUP.name) {
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screens.MAP.name) {
            val authViewModel: AuthViewModel = koinViewModel()
            val user = authViewModel.authState.collectAsState().value

            user?.uid?.let { userId ->
                MapScreen(
                    userId = userId,
                    onCountryClicked = { countryCode ->
                        navController.navigate("country/$countryCode")
                    }
                )
            }
        }


        composable("${Screens.COUNTRY.name}/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("countryCode")?.let { countryCode ->
                // CountryScreen(countryCode)
            }
        }
    }
}