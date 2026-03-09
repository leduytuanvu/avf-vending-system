package com.avf.vending.payment.impl

import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.api.PaymentType
import com.avf.vending.remote.api.WalletApiService
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Polls wallet API every 3s × 60 times = 3 minutes timeout
 */
class WalletPaymentProcessor @Inject constructor(
    private val walletApi: WalletApiService,
) : PaymentProcessor {

    override val type = PaymentType.QR_WALLET
    private var sessionId: String? = null

    override fun startSession(requiredAmount: Long): Flow<PaymentEvent> = flow {
        val qrResponse = walletApi.createQR(requiredAmount)
        sessionId = qrResponse.sessionId
        emit(PaymentEvent.WalletScanReady(qrResponse.qrString, qrResponse.deepLink))

        repeat(60) {
            delay(3_000)
            currentCoroutineContext().ensureActive()
            val status = walletApi.checkStatus(qrResponse.sessionId)
            when (status.state) {
                "PAID" -> {
                    emit(PaymentEvent.WalletConfirmed(status.transactionId ?: "", status.amount ?: 0))
                    return@flow
                }
                "EXPIRED" -> {
                    emit(PaymentEvent.WalletTimeout)
                    return@flow
                }
            }
        }
        emit(PaymentEvent.WalletTimeout)
    }

    override suspend fun cancel() { /* server-side cancel via API */ }
    override suspend fun confirm() {}
}
