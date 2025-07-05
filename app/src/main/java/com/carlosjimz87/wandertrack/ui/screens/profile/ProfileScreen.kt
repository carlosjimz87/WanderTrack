package com.carlosjimz87.wandertrack.ui.screens.profile

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.domain.models.Achievement
import com.carlosjimz87.wandertrack.domain.models.ProfileData
import com.carlosjimz87.wandertrack.ui.composables.auth.PrimaryButton
import com.carlosjimz87.wandertrack.ui.composables.profile.AchievementItem
import com.carlosjimz87.wandertrack.ui.composables.profile.StatItem
import com.carlosjimz87.wandertrack.ui.screens.auth.viewmodel.AuthViewModel
import com.carlosjimz87.wandertrack.ui.screens.profile.viewmodel.ProfileViewModel
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val profile = profileViewModel.profileState

    ProfileScreenContent(
        profile = profile,
        onEditProfile = { /* TODO Edit */ },
        onLogout = { authViewModel.logout() },
        logoutText = context.getString(R.string.logout)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreenContent(
    profile: ProfileData,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    logoutText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👩‍🦰", fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(profile.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
                Text("Edit profile", modifier = Modifier.clickable { onEditProfile() }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        Text("Stats", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onBackground)

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            StatItem("${profile.countriesVisited}", "Countries")
            StatItem("${profile.citiesVisited}", "Cities")
            StatItem("${profile.continentsVisited}", "Continents")
            StatItem("${profile.worldPercent}%", "World")
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        Text("Achievements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onBackground)

        if (profile.achievements.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Can adjust based on content
            ) {
                items(profile.achievements) { achievement ->
                    AchievementItem(achievement)
                }
            }
        } else {
            Text(
                text = "No achievements yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = logoutText,
            onClick = onLogout
        )
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
            logoutText = "Logout"
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = 0xFF121212)
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
            logoutText = "Logout"
        )
    }
}
