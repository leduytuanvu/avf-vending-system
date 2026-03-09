package com.avf.vending.feature.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.components.PriceText

@Composable
fun CashPaymentPanel(
    requiredAmount: Long,
    insertedAmount: Long,
    modifier: Modifier = Modifier,
) {
    val remaining = (requiredAmount - insertedAmount).coerceAtLeast(0L)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Insert cash",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LabeledAmount(label = "Required", amount = requiredAmount)
            LabeledAmount(label = "Inserted", amount = insertedAmount)
            LabeledAmount(label = "Remaining", amount = remaining)
        }
        if (insertedAmount > 0) {
            LinearProgressIndicator(
                progress = { (insertedAmount.toFloat() / requiredAmount.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
        }
    }
}

@Composable
private fun LabeledAmount(label: String, amount: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        PriceText(price = amount)
    }
}
