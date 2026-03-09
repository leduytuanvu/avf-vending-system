package com.avf.vending.feature.payment

import com.avf.vending.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Finite State Machine governing valid payment flow transitions.
 *
 * States:
 *   IDLE → SELECTING_METHOD → AWAITING_PAYMENT → PROCESSING → COMPLETED | FAILED | REFUNDING
 *
 * All transitions are guarded; illegal transitions are silently ignored so
 * the UI can never put the machine into an invalid state.
 */
enum class PaymentFsmState {
    IDLE,
    SELECTING_METHOD,
    AWAITING_PAYMENT,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDING,
    CANCELLED,
}

class PaymentStateMachine @Inject constructor() {
    private val lock = Any()

    private val _state = MutableStateFlow(PaymentFsmState.IDLE)
    val state: StateFlow<PaymentFsmState> = _state.asStateFlow()

    val current: PaymentFsmState get() = _state.value

    /** Start a new payment session — only valid from IDLE. */
    fun start(): Boolean = transition(PaymentFsmState.IDLE, PaymentFsmState.SELECTING_METHOD)

    /** User has chosen a payment method — only valid from SELECTING_METHOD. */
    fun methodSelected(): Boolean =
        transition(PaymentFsmState.SELECTING_METHOD, PaymentFsmState.AWAITING_PAYMENT)

    /** Sufficient funds received — only valid from AWAITING_PAYMENT. */
    fun fundsReceived(): Boolean =
        transition(PaymentFsmState.AWAITING_PAYMENT, PaymentFsmState.PROCESSING)

    /** Backend / hardware confirmed — only valid from PROCESSING. */
    fun complete(): Boolean = transition(PaymentFsmState.PROCESSING, PaymentFsmState.COMPLETED)

    /** Something went wrong — valid from AWAITING_PAYMENT or PROCESSING. */
    fun fail(): Boolean = synchronized(lock) {
        when (_state.value) {
            PaymentFsmState.AWAITING_PAYMENT,
            PaymentFsmState.PROCESSING,
            PaymentFsmState.SELECTING_METHOD,
            -> {
                _state.value = PaymentFsmState.FAILED
                true
            }
            else -> false
        }
    }

    /** User cancels — valid from SELECTING_METHOD or AWAITING_PAYMENT. */
    fun cancel(): Boolean = synchronized(lock) {
        when (_state.value) {
            PaymentFsmState.SELECTING_METHOD,
            PaymentFsmState.AWAITING_PAYMENT,
            -> {
                _state.value = PaymentFsmState.CANCELLED
                true
            }
            else -> false
        }
    }

    /** Refund initiated — only valid from FAILED or COMPLETED (edge case overpay). */
    fun startRefund(): Boolean = synchronized(lock) {
        when (_state.value) {
            PaymentFsmState.FAILED,
            PaymentFsmState.COMPLETED,
            -> {
                _state.value = PaymentFsmState.REFUNDING
                true
            }
            else -> false
        }
    }

    /** Refund complete — only valid from REFUNDING. */
    fun refundComplete(): Boolean = transition(PaymentFsmState.REFUNDING, PaymentFsmState.CANCELLED)

    /** Reset back to IDLE for the next transaction. */
    fun reset() {
        synchronized(lock) {
            _state.value = PaymentFsmState.IDLE
        }
    }

    private fun transition(from: PaymentFsmState, to: PaymentFsmState): Boolean {
        return synchronized(lock) {
            if (_state.value == from) {
                _state.value = to
                true
            } else false
        }
    }
}
