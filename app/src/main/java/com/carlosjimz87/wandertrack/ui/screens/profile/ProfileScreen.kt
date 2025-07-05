package com.carlosjimz87.wandertrack.ui.screens.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
    val profile = profileViewModel.profileState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Replace with real image logic if available
                Text("👩‍🦰", fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(profile.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge)
                Text("Edit profile", modifier = Modifier.clickable { /* TODO Edit */ }, style = MaterialTheme.typography.headlineSmall)
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        Text("Stats", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 12.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            StatItem("${profile.countriesVisited}", "Countries")
            StatItem("${profile.citiesVisited}", "Cities")
            StatItem("${profile.continentsVisited}", "Continents")
            StatItem("${profile.worldPercent}%", "World")
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        Text("Achievements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 12.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            profile.achievements.forEach {
                AchievementItem(it)
            }
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = context.getString(R.string.logout),
            onClick = {
                authViewModel.logout()
            }
        )
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun ProfileScreenPreview() {
    WanderTrackTheme {
        ProfileScreen()
    }
}