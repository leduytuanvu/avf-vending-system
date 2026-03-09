package com.avf.vending.feature.storefront.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.avf.vending.ui.components.BadgeStatus
import com.avf.vending.ui.components.StatusBadge

@Composable
fun StockBadge(stock: Int, modifier: Modifier = Modifier) {
    val (label, status) = when {
        stock == 0 -> "Out of Stock" to BadgeStatus.ERROR
        stock <= 3 -> "Low Stock: $stock" to BadgeStatus.WARNING
        else -> "In Stock: $stock" to BadgeStatus.OK
    }
    StatusBadge(label = label, status = status, modifier = modifier)
}
