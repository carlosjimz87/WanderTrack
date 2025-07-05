package com.carlosjimz87.wandertrack.ui.composables.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.domain.models.Achievement

@Composable
fun AchievementItem(achievement: Achievement) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFFFE5E5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(achievement.title, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(achievement.description, color = Color.Black, style = MaterialTheme.typography.bodySmall, maxLines = 2)
    }
}