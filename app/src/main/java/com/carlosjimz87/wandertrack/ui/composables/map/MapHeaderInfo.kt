package com.carlosjimz87.wandertrack.ui.composables.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.map.Country


@Composable
fun MapHeaderInfo(
    selectedCountry: Country?,
    visitedCountriesCount: Int,
    visitedCitiesCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(top = 16.dp, start = 16.dp)
            .wrapContentHeight()
    ) {
        if (selectedCountry != null) {
            Text(
                text = selectedCountry.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            val citiesText = if (visitedCitiesCount > 0) {
                context.getString(R.string.visited_cities, visitedCitiesCount)
            } else {
                context.getString(R.string.no_cities)
            }

            Text(
                text = citiesText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {
            Text(
                text = context.getString(R.string.your_trips),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = context.resources.getQuantityString(
                    R.plurals.countries_visited,
                    visitedCountriesCount,
                    visitedCountriesCount
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}