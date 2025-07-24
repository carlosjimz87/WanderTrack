package com.carlosjimz87.wandertrack.ui.composables.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.carlosjimz87.wandertrack.R

@Composable
fun DestructiveActionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_your_account)) },
        text = { Text(stringResource(R.string.account_permanently_deleted_msg)) },
        confirmButton = {
            AlertButton(
                textId = R.string.delete,
                onClick = onConfirm,
                destructive = true
            )
        },
        dismissButton = {
            AlertButton(
                textId = R.string.cancel,
                onClick = onDismiss
            )
        }
    )
}
