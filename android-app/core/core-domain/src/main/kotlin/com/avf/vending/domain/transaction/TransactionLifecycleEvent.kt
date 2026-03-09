package com.avf.vending.domain.transaction

sealed interface TransactionLifecycleEvent {
    data object PaymentStarted : TransactionLifecycleEvent
    data object PaymentAuthorized : TransactionLifecycleEvent
    data object PaymentFailed : TransactionLifecycleEvent
    data object DispenseStarted : TransactionLifecycleEvent
    data object DispenseSucceeded : TransactionLifecycleEvent
    data object DispenseFailed : TransactionLifecycleEvent
    data object DispenseTimedOut : TransactionLifecycleEvent
    data object RecoveredAsCompleted : TransactionLifecycleEvent
    data object RecoveredAsRefundRequired : TransactionLifecycleEvent
    data object RefundCompleted : TransactionLifecycleEvent
}
