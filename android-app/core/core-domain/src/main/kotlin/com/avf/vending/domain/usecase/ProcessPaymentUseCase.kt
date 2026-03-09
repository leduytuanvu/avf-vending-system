package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.repository.PaymentRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(payment: Payment): String = paymentRepository.insert(payment)
}
