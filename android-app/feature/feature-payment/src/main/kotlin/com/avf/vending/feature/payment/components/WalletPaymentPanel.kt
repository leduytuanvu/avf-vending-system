package com.avf.vending.feature.payment.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WalletPaymentPanel(
    qrData: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Scan QR code to pay",
            style = MaterialTheme.typography.titleMedium,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp),
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                qrData != null -> {
                    // QR rendering handled by host via qrData string
                    // A production implementation would use a QR library (e.g. zxing-android-embedded)
                    Text(
                        text = "QR: $qrData",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                    )
                }
                else -> Text(
                    text = "Generating QR…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }
        }
        Text(
            text = "Open your wallet app and scan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
