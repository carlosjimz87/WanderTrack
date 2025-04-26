package com.carlosjimz87.wandertrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(onAuthSuccess = {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("home") {
            MapScreen(
                onCountryClicked = { countryCode ->
                    navController.navigate("country/$countryCode")
                }
            )
        }

        composable(
            route = "country/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode")
            if (countryCode != null) {
                // CountryScreen(countryCode)
            }
        }
    }
}
