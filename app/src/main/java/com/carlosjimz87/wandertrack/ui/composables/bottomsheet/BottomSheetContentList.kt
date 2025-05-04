package com.carlosjimz87.wandertrack.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carlosjimz87.wandertrack.domain.models.Country

@Composable
fun BottomSheetContentList(
    country: Country,
    onToggleVisited: (String) -> Unit,
    visitedCities: Set<String>,
    onToggleCityVisited: (String) -> Unit
) {
    country.cities.forEach { city ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleVisited(city.name) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = visitedCities.contains(city.name),
                onCheckedChange = {
                    onToggleCityVisited(city.name)
                }
            )
        }
    }
}
