package com.carlosjimz87.wandertrack.ui.screens.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.SetBottomBarColor
import com.carlosjimz87.wandertrack.ui.composables.auth.AuthButtons
import com.carlosjimz87.wandertrack.ui.composables.auth.PlanetAndLogo
import com.carlosjimz87.wandertrack.ui.composables.auth.calculateResponsiveFontSize
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun AuthScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val context = LocalContext.current
        val screenHeight = this@BoxWithConstraints.maxHeight
        val screenWidth = this@BoxWithConstraints.maxWidth

        context.SetBottomBarColor()

        val titleFontSize = calculateResponsiveFontSize(screenWidth)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -(screenHeight * 0.1f))
            ) {

                PlanetAndLogo(screenWidth, screenHeight)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = titleFontSize,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = titleFontSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            // Buttons
            AuthButtons(
                onGetStartedClick = onGetStartedClick,
                onSignInClick = onSignInClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun AuthScreenPreviewLight() {
    WanderTrackTheme {
        AuthScreen(
            onGetStartedClick = {},
            onSignInClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF000000
)
@Composable
fun AuthScreenPreviewDark() {
    WanderTrackTheme {
        AuthScreen(
            onGetStartedClick = {},
            onSignInClick = {}
        )
    }
}