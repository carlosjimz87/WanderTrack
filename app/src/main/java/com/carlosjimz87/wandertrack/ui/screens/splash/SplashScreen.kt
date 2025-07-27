package com.carlosjimz87.wandertrack.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.screens.splash.viewmodel.SplashViewModel
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.carlosjimz87.wandertrack.ui.theme.AccentPinkDark
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    minSplashTimeMs: Long = 1000L,
) {
    val splashViewModel: SplashViewModel = koinViewModel()

    val animationOffset = (-50).dp
    val logoOffset = animationOffset - 90.dp
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()

    context.SetBottomBarColor(AccentPinkDark)

    // Lottie composition y animación
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = splashViewModel.isPlaying,
        speed = 1.0f,
        restartOnPlay = false
    )

    // Escala para animación del planeta
    val scale = remember { Animatable(0f) }

    // Opacidad animada para logo y texto
    val alpha = animateFloatAsState(
        targetValue = if (splashViewModel.showLogoAndText) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    // Control de animación y carga
    LaunchedEffect(Unit) {
        splashViewModel.splashStartTime = System.currentTimeMillis()

        scale.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )

        delay(minSplashTimeMs) // Espera a que Lottie termine

        splashViewModel.showLogoAndText = true

        splashViewModel.isPlaying = false // Detiene animación Lottie

        splashViewModel.loadData(context) { countries ->
            if (countries.isNotEmpty()) {
                onSplashFinished()
            }
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) AccentPink else AccentPinkDark),
        contentAlignment = Alignment.Center
    ) {

        LottieAnimation(
            composition,
            progress,
            modifier = Modifier
                .offset(y = animationOffset)
                .size(200.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
        )

        if (splashViewModel.showLogoAndText) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = context.getString(R.string.app_name),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .offset(y = logoOffset)
                    .alpha(alpha.value)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 260.dp)
                    .alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}