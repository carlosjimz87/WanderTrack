package com.carlosjimz87.wandertrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screens.SPLASH.name) {

        composable(Screens.SPLASH.name) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Screens.AUTH.name) {
                    popUpTo(Screens.SPLASH.name) { inclusive = true }
                }
            })
        }

        composable(Screens.AUTH.name) {
            AuthScreen(
                onGetStartedClick = { navController.navigate(Screens.SIGNUP.name) },
                onSignInClick = { navController.navigate(Screens.LOGIN.name) }
            )
        }

        composable(Screens.LOGIN.name) {
            val authViewModel: AuthViewModel = koinViewModel()
            val navController = rememberNavController() // o pasa el navController desde arriba si ya lo tienes

            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screens.MAP.name) {
                        popUpTo(Screens.LOGIN.name) { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    // Lógica para iniciar Google Sign-In, por ejemplo:
                    // authViewModel.startGoogleSignIn() o lanza el intent necesario
                },
                onForgotPasswordClick = {
                    // Navegar a la pantalla de recuperación de contraseña, si existe
                   // navController.navigate(Screens.FORGOT_PASSWORD.name)
                },
                authViewModel = authViewModel
            )
        }

        composable(Screens.SIGNUP.name) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screens.MAP.name) {
                        popUpTo(Screens.SIGNUP.name) { inclusive = true }
                    }
                },
                onBackToLoginClick = { navController.popBackStack() }
            )
        }

        composable(Screens.MAP.name) {
            MapScreen(
                onCountryClicked = { countryCode ->
                    navController.navigate("country/$countryCode")
                }
            )
        }

        composable(
            route = "${Screens.COUNTRY.name}/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode")
            if (countryCode != null) {
                // CountryScreen(countryCode)
            }
        }
    }
}
