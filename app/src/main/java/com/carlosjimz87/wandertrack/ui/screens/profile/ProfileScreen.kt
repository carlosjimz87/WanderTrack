package com.carlosjimz87.wandertrack.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.composables.profile.ProfileScreenContent
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.authState.collectAsState()
    val profile = profileViewModel.profileState

    context.SetBottomBarColor()
    BackHandler(onBack = onBack)

    LaunchedEffect(user) {
        if (user == null) {
            onLogout()
        }
    }

    ProfileScreenContent(
        profile = profile,
        onEditProfile = { /* TODO Edit */ },
        onLogout = { authViewModel.logout() },
        logoutText = context.getString(R.string.logout),
        avatarUrl = profileViewModel.avatarUrl,
    )
}