package com.carlosjimz87.wandertrack.ui.composables.profile


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.domain.models.profile.Achievement
import com.carlosjimz87.wandertrack.ui.theme.WanderTrackTheme

@Composable
fun AchievementItem(achievement: Achievement) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                Toast.makeText(context, achievement.description, Toast.LENGTH_SHORT).show()
            }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                achievement.title,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(
    name = "AchievementItem Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun AchievementItemPreviewLight() {
    WanderTrackTheme {
        AchievementItem(
            achievement = Achievement(
                title = "✅",
                description = "Completed your first trip!"
            )
        )
    }
}

@Preview(
    name = "AchievementItem Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AchievementItemPreviewDark() {
    WanderTrackTheme {
        AchievementItem(
            achievement = Achievement(
                title = "✅",
                description = "Completed your first trip!"
            )
        )
    }
}