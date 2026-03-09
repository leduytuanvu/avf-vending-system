package com.avf.vending.payment.impl

import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.api.PaymentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import java.util.UUID

class MockWalletProcessor @Inject constructor() : PaymentProcessor {
    override val type = PaymentType.QR_WALLET

    override fun startSession(requiredAmount: Long): Flow<PaymentEvent> = flow {
        emit(PaymentEvent.WalletScanReady("mock-qr-data", "avf://pay?mock=1"))
        delay(3000)
        emit(PaymentEvent.WalletConfirmed(UUID.randomUUID().toString(), requiredAmount))
    }

    override suspend fun cancel() {}
    override suspend fun confirm() {}
}
