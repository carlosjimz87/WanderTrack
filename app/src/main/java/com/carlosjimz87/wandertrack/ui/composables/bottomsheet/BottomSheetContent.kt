package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.domain.models.City
import com.carlosjimz87.wandertrack.ui.theme.AccentPink
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    countryName:String,
    countryCode: String,
    countryVisited: Boolean,
    countryCities: List<City>,
    onToggleCityVisited: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val buttonText = if (countryVisited) "Visitar" else "Visitado"
    val buttonColor = if (countryVisited) AccentPink else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BottomSheetContentCloseButton {
            onDismiss()
        }

        BottomSheetContentHeader(countryName, countryCode, onToggleVisited, buttonColor, buttonText)

        BottomSheetContentDivider("Cities")

        BottomSheetContentList(countryCities, onToggleCityVisited)
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