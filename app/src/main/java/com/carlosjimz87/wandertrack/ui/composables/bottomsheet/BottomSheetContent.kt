package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.common.Constants
import com.carlosjimz87.wandertrack.common.visited
import com.carlosjimz87.wandertrack.domain.models.map.City
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun CountryBottomSheetContent(
    countryName: String,
    countryCode: String,
    countryVisited: Boolean,
    countryCities: List<City>,
    onToggleCityVisited: (String) -> Unit,
    onToggleVisited: (String) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val visitedCities by remember(countryCities) {
        mutableStateOf(countryCities.visited())
    }
    val previewCities by remember(visitedCities) {
        mutableStateOf(visitedCities.take(3))
    }
    val toggleCity: (City) -> Unit = { onToggleCityVisited(it.name) }
    val hasMoreThanThreeCities = remember { countryCities.size > 3 || visitedCities.size > 3 }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .animateContentSize()
    ) {
        Text(
            text = countryName,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        VisitedButton(onToggleVisited, countryCode, countryVisited)

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = expanded,
            label = "City List Expand/Collapse",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { isExpanded ->
            if (isExpanded) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    itemsIndexed(countryCities, key = { _, city -> city.name }) { index, city ->
                        CityRow(
                            cityName = city.name,
                            isVisited = city.visited,
                            onToggle = { toggleCity(city) }
                        )
                        if (index != countryCities.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            } else {
                CompactCityRow(previewCities)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (hasMoreThanThreeCities) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { expanded = !expanded }
            ) {
                Text(
                    text = if (expanded) context.getString(R.string.show_less) else context.getString(R.string.show_more),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Preview
@Composable
private fun CountryBottomSheetContentPreview() {
    WanderTrackTheme {
        val country = Constants.countries[0]
        CountryBottomSheetContent(
            countryName = country.name,
            countryCode = country.code,
            countryVisited = country.visited,
            countryCities = country.cities,
            onToggleCityVisited = {},
            onToggleVisited = {}
        )
    }
}


@Preview
@Composable
private fun CountryBottomSheetContentPreview2() {
    WanderTrackTheme {
        val country = Constants.countries[1]
        CountryBottomSheetContent(
            countryName = country.name,
            countryCode = country.code,
            countryVisited = country.visited,
            countryCities = country.cities,
            onToggleCityVisited = {},
            onToggleVisited = {}
        )
    }
}