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
    profileViewModel.loadProfile(authViewModel.userName)
    val profile = profileViewModel.profileState

    var showDeleteDialog by remember { mutableStateOf(false) }

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
        onDeleteAccountClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        DestructiveActionDialog(
            onConfirm = {
                showDeleteDialog = false
                authViewModel.deleteAccount { success, message ->
                    if (success) {
                        onLogout()
                        Toast.makeText(
                            context,
                            context.getString(R.string.account_deleted_successfully),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            message ?: context.getString(R.string.error_deleting_account),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}