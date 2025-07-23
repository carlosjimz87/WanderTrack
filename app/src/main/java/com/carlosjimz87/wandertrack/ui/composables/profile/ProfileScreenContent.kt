package com.carlosjimz87.wandertrack.ui.composables.profile


import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.profile.Achievement
import com.carlosjimz87.wandertrack.domain.models.profile.ProfileData
import com.carlosjimz87.wandertrack.ui.composables.auth.PrimaryButton
import com.carlosjimz87.wandertrack.ui.composables.auth.calculateResponsiveFontSize
import com.carlosjimz87.wandertrack.ui.theme.AccentPinkDark
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    profile: ProfileData,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    logoutText: String,
    avatarUrl: String? = null,
    onDeleteAccountClick: () -> Unit
) {
    val context = LocalContext.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(24.dp)
    ) {
        val screenWidth = this@BoxWithConstraints.maxWidth
        val avatarSize = (screenWidth * 0.15f).coerceIn(64.dp, 96.dp)
        val titleFontSize = calculateResponsiveFontSize(screenWidth)

        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.profile),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(size = avatarSize, fontSize = titleFontSize, url = avatarUrl)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        profile.username,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        stringResource(R.string.edit_profile),
                        modifier = Modifier.clickable { onEditProfile() },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text(
                stringResource(R.string.stats),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("${profile.countriesVisited}", stringResource(R.string.countries))
                StatItem("${profile.citiesVisited}", stringResource(R.string.cities))
                StatItem("${profile.continentsVisited}", stringResource(R.string.continents))
                StatItem("${profile.worldPercent}%", stringResource(R.string.world))
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text(
                stringResource(R.string.achievements),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (profile.achievements.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(profile.achievements) { achievement ->
                        AchievementItem(achievement)
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_achievements_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDeleteAccountClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_account),
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.delete_account), style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(4.dp))

            PrimaryButton(
                text = logoutText,
                onClick = onLogout
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun ProfileScreenPreviewAchievements() {
    WanderTrackTheme {
        ProfileScreenContent(
            profile = ProfileData(
                username = "Explorer123",
                countriesVisited = 13,
                citiesVisited = 20,
                continentsVisited = 4,
                worldPercent = 7,
                achievements = listOf(
                    Achievement("🚩", "First country visited"),
                    Achievement("5️⃣", "5 countries reached"),
                    Achievement("🔟", "10 countries reached"),
                    Achievement("🌍", "20 countries reached"),
                    Achievement("🎯", "Completed a country"),
                    Achievement("🏙️", "First city visited"),
                    Achievement("🔟🏙️", "10 cities reached"),
                    Achievement("🏙️⭐", "50 cities explored"),
                    Achievement("🌎", "Visited all continents")
                )
            ),
            onEditProfile = {},
            onLogout = {},
            logoutText = "Logout",
            onDeleteAccountClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF121212
)
@Composable
fun ProfileScreenPreviewAchievementsDark() {
    WanderTrackTheme {
        ProfileScreenContent(
            profile = ProfileData(
                username = "Explorer123",
                countriesVisited = 13,
                citiesVisited = 20,
                continentsVisited = 4,
                worldPercent = 7,
                achievements = listOf(
                    Achievement("🚩", "First country visited"),
                    Achievement("5️⃣", "5 countries reached"),
                    Achievement("🔟", "10 countries reached"),
                    Achievement("🌍", "20 countries reached"),
                    Achievement("🎯", "Completed a country"),
                    Achievement("🏙️", "First city visited"),
                    Achievement("🔟🏙️", "10 cities reached"),
                    Achievement("🏙️⭐", "50 cities explored"),
                    Achievement("🌎", "Visited all continents")
                )
            ),
            onEditProfile = {},
            onLogout = {},
            logoutText = "Logout",
            onDeleteAccountClick = {}
        )
    }
}