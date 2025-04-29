package com.carlosjimz87.wandertrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.mapscreen.SimpleMapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screens.MAP.name) {
        composable(Screens.AUTH.name) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screens.MAP.name) {
                    popUpTo(Screens.AUTH.name) { inclusive = true }
                }
            })
        }
        composable(Screens.TEST.name) {
            SimpleMapScreen()
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
