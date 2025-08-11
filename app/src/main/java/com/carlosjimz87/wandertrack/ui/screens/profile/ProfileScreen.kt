package com.carlosjimz87.wandertrack.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.composables.profile.DestructiveActionDialog
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
    val uiState by authViewModel.authUiState.collectAsState()
    val profile = profileViewModel.profileState
    profileViewModel.loadProfile()
    var showDeleteDialog by remember { mutableStateOf(false) }

    context.SetBottomBarColor()
    BackHandler(onBack = onBack)

    LaunchedEffect(user) {
        if (user == null) {
            onLogout()
        }
    }

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            authViewModel.clearUiState()
            onLogout()
            Toast.makeText(
                context,
                context.getString(R.string.account_deleted_successfully),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    ProfileScreenContent(
        profile = profile,
        onEditProfile = { /* TODO Edit */ },
        onLogout = {
            authViewModel.logout()
            authViewModel.clearUiState()
        },
        avatarUrl = profile.avatarUrl,
        onDeleteAccountClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        DestructiveActionDialog(
            onConfirm = {
                showDeleteDialog = false
                authViewModel.deleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}