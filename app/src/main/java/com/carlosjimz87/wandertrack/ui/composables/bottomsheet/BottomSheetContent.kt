package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    countryName: String,
    countryCode: String,
    countryVisited: Boolean,
    countryCities: List<City>,
    onToggleCityVisited: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val previewCities = if (expanded) countryCities else countryCities.take(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {

        // Nombre del país centrado
        Text(
            text = countryName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón visitar centrado y destacado
        Button(
            onClick = { onToggleVisited(countryCode) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .height(48.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (countryVisited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (countryVisited) "Visitado" else "Visitar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista scrollable de ciudades
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
        ) {
            items(previewCities) { city ->
                CityRow(
                    cityName = city.name,
                    isVisited = city.visited,
                    onToggle = { onToggleCityVisited(city.name) }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
            if (countryCities.size > 3) {
                item {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Mostrar menos" else "Mostrar más")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CountryBottomSheetContentPreview() {
    WanderTrackTheme {
        val country = Constants.countries.first()
        CountryBottomSheetContent(
            countryName = country.name,
            countryCode = country.code,
            countryVisited = country.visited,
            countryCities = country.cities,
            onToggleCityVisited = {},
            onToggleVisited = {},
            onDismiss = {}
        )
    }
}