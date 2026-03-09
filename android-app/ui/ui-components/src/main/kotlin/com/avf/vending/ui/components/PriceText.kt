package com.avf.vending.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PriceText(
    price: Long,
    modifier: Modifier = Modifier,
) {
    val formatted = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(price)
    Text(
        text = "$formatted đ",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
