package com.carlosjimz87.wandertrack.ui.composables.auth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun MessageDialog(
    title: String = "Notice",
    message: String = "This is a message dialog.",
    onDismiss: () -> Unit
    ) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}