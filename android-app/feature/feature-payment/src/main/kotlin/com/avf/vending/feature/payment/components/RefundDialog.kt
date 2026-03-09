package com.avf.vending.feature.payment.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.avf.vending.ui.components.PriceText

@Composable
fun RefundDialog(
    changeAmount: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Change") },
        text = {
            Text("Please collect your change of ")
            PriceText(price = changeAmount)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
