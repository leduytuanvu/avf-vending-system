package com.avf.vending.payment.api

sealed class PaymentEvent {
    data class CashInserted(val denomination: Long, val total: Long) : PaymentEvent()
    data class CashSufficient(val total: Long, val change: Long) : PaymentEvent()
    data class CashRejected(val reason: String) : PaymentEvent()
    data class ChangeDispensed(val amount: Long) : PaymentEvent()
    data class WalletScanReady(val qrData: String, val deepLink: String) : PaymentEvent()
    data class WalletConfirmed(val transactionId: String, val amount: Long) : PaymentEvent()
    object WalletTimeout : PaymentEvent()
    data class WalletFailed(val reason: String) : PaymentEvent()
}
