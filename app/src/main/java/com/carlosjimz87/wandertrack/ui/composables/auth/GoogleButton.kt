package com.carlosjimz87.wandertrack.ui.composables.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carlosjimz87.wandertrack.R
import com.carlosjimz87.wandertrack.ui.theme.Black

@Composable
fun GoogleButton(
    onGoogleSignInClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedButton(
        modifier = modifier
            .fillMaxWidth().height(56.dp),
        onClick = onGoogleSignInClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google),
            contentDescription = context.getString(R.string.google_logo),
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified // This preserves original icon colors
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(context.getString(R.string.continueText))
                }
                append(context.getString(R.string.withGoogle))
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GoogleButtonPreview() {
    GoogleButton(onGoogleSignInClick = {})
}