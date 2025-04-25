package com.carlosjimz87.wandertrack.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {

    Card(
        modifier = Modifier.padding(horizontal = 20.dp).clickable {
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Google Sign-In",
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        )

    }
}