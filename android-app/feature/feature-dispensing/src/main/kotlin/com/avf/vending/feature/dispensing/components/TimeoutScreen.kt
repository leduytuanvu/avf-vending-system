package com.avf.vending.feature.dispensing.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.theme.VendingAmber

@Composable
fun DispensingTimeoutScreen(
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier.fillMaxSize().padding(32.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = VendingAmber,
            modifier = Modifier.size(80.dp),
        )
        Text(
            text = "Session Timed Out",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "The dispensing operation timed out. Please contact support if your product was not dispensed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
            Text("Return to Home")
        }
    }
}
