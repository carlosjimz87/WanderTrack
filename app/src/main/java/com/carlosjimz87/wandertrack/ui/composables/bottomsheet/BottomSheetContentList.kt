package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.carlosjimz87.wandertrack.domain.models.City

@Composable
fun BottomSheetContentList(
    cities: List<City>,
    onToggleCityVisited: (String) -> Unit
) {

    Column {
        cities.forEach { city ->
            key(city.name) { // Prevent reusing the same recomposition scope
                CityRow(
                    cityName = city.name,
                    isVisited = city.visited,
                    onToggle = { onToggleCityVisited(city.name) }
                )
            }
        }
    }
}
