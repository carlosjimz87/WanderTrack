package com.carlosjimz87.wandertrack.ui.composables.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.carlosjimz87.wandertrack.R

@Composable
fun PlanetAndLogo(
    screenWidth: Dp,
    screenHeight: Dp
) {
    Box(
        modifier = Modifier.offset(y = -(screenHeight * 0.05f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.planet),
            contentDescription = null,
            modifier = Modifier
                .size(screenWidth * 0.7f)
                .align(Alignment.Center)
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(screenWidth * 0.25f)
                .align(Alignment.TopCenter)
                .offset(y = -(screenWidth * 0.025f))
        )
    }
}