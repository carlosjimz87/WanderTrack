package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable

@Composable
fun VisitedCitySwitch(isVisited: Boolean, onToggle: () -> Unit) {
    Switch(
        checked = isVisited,
        onCheckedChange = { onToggle() },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,           // White or Black depending on mode
            checkedTrackColor = MaterialTheme.colorScheme.primary,             // AccentPink or AccentPinkDark
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Soft gray thumb
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Soft blue-gray track
        )
    )
}