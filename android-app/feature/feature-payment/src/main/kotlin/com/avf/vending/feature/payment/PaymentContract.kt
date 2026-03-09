package com.avf.vending.feature.payment

import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.Product

data class PaymentState(
    val product: Product? = null,
    val slotId: String = "",
    val requiredAmount: Long = 0L,
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    val insertedAmount: Long = 0L,
    val change: Long = 0L,
    val qrData: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
)

sealed class PaymentIntent {
    data class Init(val slotId: String, val productId: String, val amount: Long) : PaymentIntent()
    data class SelectMethod(val method: PaymentMethod) : PaymentIntent()
    object Cancel : PaymentIntent()
    object Confirm : PaymentIntent()
}

sealed class PaymentEffect {
    data class NavigateToDispensing(val transactionId: String, val slotAddress: String) : PaymentEffect()
    object NavigateBack : PaymentEffect()
    data class ShowError(val message: String) : PaymentEffect()
}
