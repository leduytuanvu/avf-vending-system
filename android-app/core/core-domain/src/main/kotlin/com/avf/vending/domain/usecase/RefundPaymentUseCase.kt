package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.repository.PaymentRepository
import javax.inject.Inject

class RefundPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(paymentId: String) {
        paymentRepository.updateStatus(paymentId, PaymentStatus.REFUNDED)
    }
}
