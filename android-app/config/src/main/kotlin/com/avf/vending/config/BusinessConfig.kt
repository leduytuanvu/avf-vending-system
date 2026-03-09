package com.avf.vending.config

data class BusinessConfig(
    val taxRate: Float = 0f,
    val receiptEnabled: Boolean = false,
    val promoEnabled: Boolean = false,
    val maxChangeDispensed: Long = 200_000L,    // VND
    val minimumPurchaseAmount: Long = 1_000L,   // VND
    val currencySymbol: String = "đ",
)
