package com.avf.vending.feature.storefront.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PriceTag(amountVnd: Long, modifier: Modifier = Modifier) {
    val formatted = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        .format(amountVnd) + "đ"
    Text(
        text = formatted,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
