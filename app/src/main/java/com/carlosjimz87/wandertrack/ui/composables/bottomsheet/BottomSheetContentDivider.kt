package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetContentDivider(title:String) {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}