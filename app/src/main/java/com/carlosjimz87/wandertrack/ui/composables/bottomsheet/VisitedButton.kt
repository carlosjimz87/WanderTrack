package com.carlosjimz87.wandertrack.ui.composables.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R

@Composable
fun ColumnScope.VisitedButton(
    onToggleVisited: (String) -> Unit,
    countryCode: String,
    countryVisited: Boolean
) {
    val context = LocalContext.current

    Button(
        onClick = { onToggleVisited(countryCode) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (countryVisited) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = if (countryVisited) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(48.dp)
            .align(Alignment.CenterHorizontally)
    ) {
        Icon(
            imageVector = if (countryVisited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = if (countryVisited) context.getString(R.string.visited) else context.getString(R.string.to_visit))
    }
}