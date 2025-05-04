package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.Country
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    country: Country,
    visitedCities: Set<String>,
    visitedCountries : List<Country>,
    onToggleCityVisited: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isVisited = visitedCountries.any { it.name == country.name }
    val buttonText = if (isVisited) "Visitar" else "Visitado"
    val buttonColor = if (isVisited) AccentPink else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BottomSheetContentCloseButton {
            onDismiss()
        }

        BottomSheetContentHeader(country, onToggleVisited, buttonColor, buttonText)

        BottomSheetContentDivider("Cities")

        BottomSheetContentList(country, onToggleVisited, visitedCities, onToggleCityVisited)
    }
}


@Composable
private fun BottomSheetContentHeader(
    country: Country,
    onToggleVisited: (String) -> Unit,
    buttonColor: Color,
    buttonText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
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

        Spacer(modifier = Modifier.width(20.dp))

        Button(
            onClick = { onToggleVisited(country.code) },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            modifier = Modifier
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(buttonText, color = Color.White)
        }
    }
}

@Preview
@Composable
private fun CountryBottomSheetContentPreview() {

    WanderTrackTheme {


        val country = Constants.countries.first()

        CountryBottomSheetContent(
            country = country,
            visitedCities = setOf("Ciudad 1", "Ciudad 2"),
            visitedCountries = listOf(country),
            onToggleCityVisited = {},
            onToggleVisited = {},
            onDismiss = {}
        )

    }
    
}