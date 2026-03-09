package com.avf.vending.payment.orchestrator

import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.api.PaymentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class PaymentOrchestrator @Inject constructor(
    private val processors: @JvmSuppressWildcards Set<PaymentProcessor>,
) {
    private val activeProcessors = mutableListOf<PaymentProcessor>()

    fun startSession(requiredAmount: Long, types: List<PaymentType> = listOf(PaymentType.CASH)): Flow<PaymentEvent> {
        activeProcessors.clear()
        activeProcessors.addAll(processors.filter { it.type in types })
        val flows = activeProcessors.map { it.startSession(requiredAmount) }
        return flows.merge()
    }

    suspend fun cancel() = activeProcessors.forEach { it.cancel() }
    suspend fun confirm() = activeProcessors.forEach { it.confirm() }
}
