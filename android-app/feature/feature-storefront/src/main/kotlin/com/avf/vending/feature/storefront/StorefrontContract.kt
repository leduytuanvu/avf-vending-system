package com.avf.vending.feature.storefront

import androidx.compose.runtime.Immutable

@Immutable
data class StorefrontItemUiModel(
    val slotId: String,
    val productId: String,
    val name: String,
    val price: Long,
    val slotAddress: String,
    val stock: Int,
)

@Immutable
data class StorefrontState(
    val items: List<StorefrontItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed class StorefrontIntent {
    object Load : StorefrontIntent()
    data class SelectProduct(
        val slotId: String,
        val productId: String,
        val amount: Long,
    ) : StorefrontIntent()
}

sealed class StorefrontEffect {
    data class NavigateToPayment(
        val slotId: String,
        val productId: String,
        val amount: Long,
    ) : StorefrontEffect()
}
