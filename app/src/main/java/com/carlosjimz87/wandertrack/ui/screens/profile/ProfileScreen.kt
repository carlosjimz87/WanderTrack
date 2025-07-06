package com.carlosjimz87.wandertrack.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.navigation.Screens
import com.carlosjimz87.wandertrack.ui.composables.profile.ProfileScreenContent
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val profile = profileViewModel.profileState
    val user by authViewModel.authState.collectAsState()

    context.SetBottomBarColor()

    // Navigate to AuthScreen when user logs out
    LaunchedEffect(user) {
        if (user == null) {
            navController.navigate(Screens.AUTH.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    ProfileScreenContent(
        profile = profile,
        onEditProfile = { /* TODO Edit */ },
        onLogout = { authViewModel.logout() },
        logoutText = context.getString(R.string.logout)
    )
}

