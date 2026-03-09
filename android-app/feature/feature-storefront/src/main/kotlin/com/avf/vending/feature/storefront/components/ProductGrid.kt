package com.avf.vending.feature.storefront.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avf.vending.feature.storefront.StorefrontItemUiModel
import com.avf.vending.ui.components.ProductCard
import com.avf.vending.ui.components.ProductCardSkeleton

@Composable
fun ProductGrid(
    items: List<StorefrontItemUiModel>,
    isLoading: Boolean,
    onProductSelected: (StorefrontItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        if (isLoading) {
            items(6) { ProductCardSkeleton() }
        } else {
            items(
                items = items,
                key = { it.slotId },
                contentType = { "product" },
            ) { productSlot ->
                ProductCard(
                    name = productSlot.name,
                    price = productSlot.price,
                    slotAddress = productSlot.slotAddress,
                    stock = productSlot.stock,
                    onClick = { onProductSelected(productSlot) },
                )
            }
        }
    }
}
