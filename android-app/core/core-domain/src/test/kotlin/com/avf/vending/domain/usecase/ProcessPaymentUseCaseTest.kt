package com.avf.vending.domain.usecase

import com.avf.vending.domain.model.Payment
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.domain.model.PaymentStatus
import com.avf.vending.domain.repository.PaymentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class ProcessPaymentUseCaseTest {

    private val paymentRepository: PaymentRepository = mock()
    private val useCase = ProcessPaymentUseCase(paymentRepository)

    private val payment = Payment(
        id = "",
        transactionId = "txn_1",
        amount = 15_000L,
        change = 0L,
        method = PaymentMethod.CASH,
        status = PaymentStatus.PENDING,
        createdAt = 0L,
    )

    @Test
    fun `invoke inserts payment and returns id without forcing status update`() = runTest {
        whenever(paymentRepository.insert(payment)).thenReturn("pay_1")

        val result = useCase(payment)

        assertEquals("pay_1", result)
        verify(paymentRepository).insert(payment)
        verify(paymentRepository, never()).updateStatus(any(), any())
    }

    @Test
    fun `invoke propagates exception from repository`() = runTest {
        whenever(paymentRepository.insert(payment)).thenThrow(RuntimeException("DB error"))

        assertThrows(RuntimeException::class.java) {
            runTest { useCase(payment) }
        }
        verify(paymentRepository, never()).updateStatus(any(), any())
    }
}
