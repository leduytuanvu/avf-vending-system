package com.avf.vending.payment.impl

import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.api.PaymentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockCashProcessor @Inject constructor() : PaymentProcessor {
    override val type = PaymentType.CASH

    override fun startSession(requiredAmount: Long): Flow<PaymentEvent> = flow {
        delay(1000)
        emit(PaymentEvent.CashInserted(requiredAmount, requiredAmount))
        delay(500)
        emit(PaymentEvent.CashSufficient(requiredAmount, 0))
    }

    override suspend fun cancel() {}
    override suspend fun confirm() {}
}
