package com.avf.vending.domain.transaction

import com.avf.vending.domain.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionStateMachineTest {
    private val stateMachine = TransactionStateMachine()

    @Test
    fun `happy path transitions payment to completed`() {
        val paymentProcessing = stateMachine.transition(
            TransactionStatus.PENDING,
            TransactionLifecycleEvent.PaymentStarted,
        )
        val paymentSuccess = stateMachine.transition(
            paymentProcessing,
            TransactionLifecycleEvent.PaymentAuthorized,
        )
        val dispensing = stateMachine.transition(
            paymentSuccess,
            TransactionLifecycleEvent.DispenseStarted,
        )
        val completed = stateMachine.transition(
            dispensing,
            TransactionLifecycleEvent.DispenseSucceeded,
        )

        assertEquals(TransactionStatus.PAYMENT_PROCESSING, paymentProcessing)
        assertEquals(TransactionStatus.PAYMENT_SUCCESS, paymentSuccess)
        assertEquals(TransactionStatus.DISPENSING, dispensing)
        assertEquals(TransactionStatus.COMPLETED, completed)
    }

    @Test
    fun `failed dispense transitions to refund required`() {
        val refundRequired = stateMachine.transition(
            TransactionStatus.DISPENSING,
            TransactionLifecycleEvent.DispenseFailed,
        )

        assertEquals(TransactionStatus.REFUND_REQUIRED, refundRequired)
    }

    @Test
    fun `illegal transitions throw`() {
        assertThrows(IllegalArgumentException::class.java) {
            stateMachine.transition(
                TransactionStatus.PENDING,
                TransactionLifecycleEvent.DispenseStarted,
            )
        }
    }
}
