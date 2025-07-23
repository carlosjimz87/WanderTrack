package com.carlosjimz87.wandertrack.ui.composables.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.composables.auth.PrimaryButton
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme
import com.carlosjimz87.wandertrack.ui.theme.White

@Composable
fun ProfileButtons(
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDeleteAccountClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_account),
                tint = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.delete_account), style = MaterialTheme.typography.headlineSmall)
        }

        PrimaryButton(
            text = stringResource(R.string.logout),
            onClick = onLogoutClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthButtonsPreview() {
    WanderTrackTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(White)
        ) {
            ProfileButtons(
                onLogoutClick = {},
                onDeleteAccountClick = {}
            )
        }
    }
}
