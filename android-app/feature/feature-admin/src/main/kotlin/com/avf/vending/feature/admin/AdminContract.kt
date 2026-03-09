package com.avf.vending.feature.admin

import com.avf.vending.domain.model.ProductSlot
import com.avf.vending.domain.model.Transaction

data class AdminState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val refundRequiredCount: Int = 0,
    val slots: List<ProductSlot> = emptyList(),
    val error: String? = null,
)

sealed class AdminIntent {
    object Load : AdminIntent()
    object Logout : AdminIntent()
}

sealed class AdminEffect {
    object NavigateBack : AdminEffect()
}
