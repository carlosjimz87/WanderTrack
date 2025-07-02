package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CityRow(
    cityName: String,
    isVisited: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = cityName, style = MaterialTheme.typography.bodyLarge)
        VisitedCitySwitch(isVisited, onToggle)
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF)
@Composable
fun CityRowActivePreview() {
    CityRow(
        cityName = "Paris",
        isVisited = true,
        onToggle = { }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF)
@Composable
fun CityRowPreview() {
    CityRow(
        cityName = "Paris",
        isVisited = false,
        onToggle = { }
    )
}