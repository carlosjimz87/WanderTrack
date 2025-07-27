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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.carlosjimz87.wandertrack.domain.models.Screens
import com.carlosjimz87.wandertrack.ui.screens.auth.AuthScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.LoginScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.SignUpScreen
import com.carlosjimz87.wandertrack.ui.screens.auth.state.AuthUiState
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.map.MapScreen
import com.carlosjimz87.wandertrack.ui.screens.profile.ProfileScreen
import com.carlosjimz87.wandertrack.ui.screens.splash.SplashScreen
import com.carlosjimz87.wandertrack.utils.Logger
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val validSession by authViewModel.validSession.collectAsState()
    val authUiState by authViewModel.authUiState.collectAsState()
    var hasFinishedSplash by remember { mutableStateOf(false) }

    val controller = rememberMyNavController(initial = Screens.Splash)
    val currentScreen = controller.current

    LaunchedEffect(validSession, authUiState, hasFinishedSplash) {
        val isSuccessLogin = authUiState is AuthUiState.Success && validSession == true
        val isInitialLaunch = hasFinishedSplash && validSession != null
        val isInvalidSession = validSession == false && currentScreen.isProtected()

        when {
            isSuccessLogin && currentScreen.isPublic() -> {
                Logger.d("NavLog -> Navigating to Map (auth success)")
                controller.replace(Screens.Map)
                authViewModel.clearAuthUiState()
            }

            isInitialLaunch -> {
                val target = if (validSession == true) Screens.Map else Screens.Auth
                Logger.d("NavLog -> Navigating to $target (post-splash)")
                controller.replace(target)
            }

            isInvalidSession -> {
                Logger.d("NavLog -> Navigating to Auth (session invalid)")
                controller.replace(Screens.Auth)
            }
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
                onSplashFinished = { hasFinishedSplash = true }
            )

            is Screens.Auth -> AuthScreen(
                onGetStartedClick = {
                    Logger.d("NavLog -> Navigating to SignUp")
                    controller.navigate(Screens.SignUp)
                },
                onSignInClick = {
                    Logger.d("NavLog -> Navigating to Login")
                    controller.navigate(Screens.Login)
                }
            )

            is Screens.Login -> LoginScreen(
                onGoogleIdTokenReceived = { idToken ->
                    authViewModel.loginWithGoogle(idToken) { success, _ ->
                        if (!success) {
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onBack = {
                    Logger.d("NavLog -> Back to Auth from Login")
                    controller.replace(Screens.Auth)
                }
            )

            is Screens.SignUp -> SignUpScreen(
                onNavigateToLogin = {
                    Logger.d("NavLog -> Navigating to Login from SignUp")
                    controller.replace(Screens.Login)
                },
                onBack = {
                    Logger.d("NavLog -> Back to Auth from SignUp")
                    controller.replace(Screens.Auth)
                }
            )

            is Screens.Map -> {
                val userId = authViewModel.userId
                if (userId != null) {
                    MapScreen(
                        userId = userId,
                        onProfileClick = {
                            Logger.d("NavLog -> Navigating to Profile")
                            controller.navigate(Screens.Profile)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                        Logger.d("NavLog -> Redirecting to Auth (userId null)")
                        controller.replace(Screens.Auth)
                    }
                }
            }

            is Screens.Profile -> ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    Logger.d("NavLog -> Logging out and going to Auth")
                    controller.setNewRoot(Screens.Auth)
                },
                onBack = {
                    val target = if (validSession == true) Screens.Map else Screens.Auth
                    Logger.d("NavLog -> Back from Profile to $target")
                    controller.replace(target)
                }
            )
        }
    }
}