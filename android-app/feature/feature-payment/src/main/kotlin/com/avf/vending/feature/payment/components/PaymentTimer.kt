package com.avf.vending.feature.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.components.CountdownTimer

@Composable
fun PaymentTimer(
    remainingMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        CountdownTimer(
            remainingMs = remainingMs,
            totalMs = totalMs,
            size = 64.dp,
            strokeWidth = 5.dp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Time remaining",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
