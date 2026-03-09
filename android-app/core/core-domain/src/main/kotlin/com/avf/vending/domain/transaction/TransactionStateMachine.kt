package com.avf.vending.domain.transaction

import com.avf.vending.domain.model.TransactionStatus
import javax.inject.Inject

class TransactionStateMachine @Inject constructor() {
    fun transition(current: TransactionStatus, event: TransactionLifecycleEvent): TransactionStatus = when (event) {
        TransactionLifecycleEvent.PaymentStarted -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PENDING),
            next = TransactionStatus.PAYMENT_PROCESSING,
        )
        TransactionLifecycleEvent.PaymentAuthorized -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PAYMENT_PROCESSING, TransactionStatus.PENDING),
            next = TransactionStatus.PAYMENT_SUCCESS,
        )
        TransactionLifecycleEvent.PaymentFailed -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PENDING, TransactionStatus.PAYMENT_PROCESSING),
            next = TransactionStatus.FAILED,
        )
        TransactionLifecycleEvent.DispenseStarted -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PAYMENT_SUCCESS),
            next = TransactionStatus.DISPENSING,
        )
        TransactionLifecycleEvent.DispenseSucceeded -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.DISPENSING, TransactionStatus.PAYMENT_SUCCESS),
            next = TransactionStatus.COMPLETED,
        )
        TransactionLifecycleEvent.DispenseFailed,
        TransactionLifecycleEvent.DispenseTimedOut,
        TransactionLifecycleEvent.RecoveredAsRefundRequired -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PAYMENT_SUCCESS, TransactionStatus.DISPENSING),
            next = TransactionStatus.REFUND_REQUIRED,
        )
        TransactionLifecycleEvent.RecoveredAsCompleted -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.PAYMENT_SUCCESS, TransactionStatus.DISPENSING),
            next = TransactionStatus.COMPLETED,
        )
        TransactionLifecycleEvent.RefundCompleted -> requireTransition(
            current = current,
            allowed = setOf(TransactionStatus.REFUND_REQUIRED),
            next = TransactionStatus.REFUNDED,
        )
    }

    private fun requireTransition(
        current: TransactionStatus,
        allowed: Set<TransactionStatus>,
        next: TransactionStatus,
    ): TransactionStatus {
        require(current in allowed) {
            "Illegal transaction transition from $current to $next"
        }
        return next
    }
}
