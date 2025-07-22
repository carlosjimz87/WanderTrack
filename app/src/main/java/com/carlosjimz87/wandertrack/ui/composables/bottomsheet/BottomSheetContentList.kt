package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.domain.models.map.City
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun BottomSheetContentList(
    cities: List<City>,
    onToggleCityVisited: (String) -> Unit
) {

    var citiesExpanded by remember { mutableStateOf(false) }

    TextButton(onClick = { citiesExpanded = !citiesExpanded }) {
        Text(if (citiesExpanded) "Hide cities" else "Show cities")
    }

    AnimatedVisibility(visible = citiesExpanded) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            items(cities) { city ->
                CityRow(
                    cityName = city.name,
                    isVisited = city.visited,
                    onToggle = { onToggleCityVisited(city.name) }
                )
            }
        }
    }
}
