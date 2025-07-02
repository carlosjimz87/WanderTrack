package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VisitedCitySwitch(isVisited: Boolean, onToggle: () -> Unit) {
    Switch(
        checked = isVisited,
        onCheckedChange = { onToggle() },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            uncheckedThumbColor = Color(0xFFB0B0B0),
            uncheckedTrackColor = Color(0xFFE0E0E0)
        )
    )
}