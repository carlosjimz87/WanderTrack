package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    countryCode: String,
    countryVisited: Boolean,
    countryCities: List<City>,
    onToggleCityVisited: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón visitar arriba derecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onToggleVisited(countryCode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (countryVisited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = if (countryVisited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (countryVisited) "Visitado" else "Visitar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista scrollable de ciudades
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
        ) {
            items(countryCities) { city ->
                CityRow(
                    cityName = city.name,
                    isVisited = city.visited,
                    onToggle = { onToggleCityVisited(city.name) }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
            countryCode = country.code,
            countryVisited = country.visited,
            countryCities = country.cities,
            onToggleCityVisited = {},
            onToggleVisited = {},
            onDismiss = {}
        )

    }
    
}