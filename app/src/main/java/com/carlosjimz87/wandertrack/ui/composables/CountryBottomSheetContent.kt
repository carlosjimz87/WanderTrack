package com.carlosjimz87.wandertrack.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    country: Country,
    onToggleVisited: (String) -> Unit,
    onCityClicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onDismiss() }) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre del país + bandera
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Default.LocationOn),
                contentDescription = "Bandera",
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Text(
                text = country.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de ciudades
        country.cities.forEach { city ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onCityClicked(city.name) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = city.visited,
                    onCheckedChange = {
                        // TODO: implementar toggle de ciudad
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun CountryBottomSheetContentPreview() {
    WanderTrackTheme {
        val country = Country(
            code = "ES",
            name = "Spain",
            cities = listOf(
                City("MAD", "Madrid", false),
                City("BCN", "Barcelona", true)
            )
        )

        CountryBottomSheetContent(
            country = country,
            onToggleVisited = {},
            onCityClicked = {},
            onDismiss = {}
        )
    }

}