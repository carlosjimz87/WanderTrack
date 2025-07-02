package com.carlosjimz87.wandertrack.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.auth.AuthButtons

@Composable
fun AuthScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        // Imagen fija que representa el fotograma final de la animación
        Image(
            painter = painterResource(id = R.drawable.planet),
            contentDescription = null,
            modifier = Modifier
                .offset(y = (-50).dp)
                .size(300.dp)
        )

        // Pin de la app sobre el planeta
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = context.getString(R.string.app_name),
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center)
                .offset(y = (-140).dp)
        )

        // Texto app name
        Text(
            text = context.getString(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 260.dp)
        )

        AuthButtons(
            onGetStartedClick = onGetStartedClick,
            onSignInClick = onSignInClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}