package com.carlosjimz87.wandertrack.ui.composables.profile


import androidx.annotation.StringRes
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.carlosjimz87.wandertrack.ui.theme.AccentPinkDark
import com.carlosjimz87.wandertrack.ui.theme.Black
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme
import com.carlosjimz87.wandertrack.ui.theme.White

@Composable
fun AlertButton(
    @StringRes textId: Int,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        colors = if (destructive) {
            ButtonDefaults.filledTonalButtonColors(
                containerColor = AccentPinkDark,
                contentColor   = White
            )
        } else {
            ButtonDefaults.filledTonalButtonColors(
                containerColor = AccentPink,
                contentColor   = Black
            )
        }
    ) {
        Text(stringResource(textId))
    }
}

@Preview(showBackground = true)
@Composable
fun AlertButtonDestructiveLightPreview() {
    WanderTrackTheme {
        AlertButton(
            textId = R.string.delete,
            onClick = {},
            destructive = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlertButtonLightPreview() {
    WanderTrackTheme {

        AlertButton(
            textId = R.string.cancel,
            onClick = {},
        )
    }
}