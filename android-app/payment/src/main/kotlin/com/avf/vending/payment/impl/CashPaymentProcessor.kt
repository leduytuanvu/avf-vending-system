package com.avf.vending.payment.impl

import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentProcessor
import com.avf.vending.payment.api.PaymentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class CashPaymentProcessor @Inject constructor(
    private val billDriver: BillValidatorDriver,
    private val hardwareEventBus: HardwareEventBus,
) : PaymentProcessor {
    private enum class SessionState { IDLE, COLLECTING, AUTHORIZED, CANCELLED, COMPLETED }

    override val type = PaymentType.CASH

    @Volatile private var requiredAmount = 0L

    // AtomicLong for safe read from cancel()/confirm() which run concurrently
    // with mapBillEvent() on the bill-event collection coroutine.
    private val totalInserted = AtomicLong(0L)

    // Serialises mapBillEvent() vs cancel()/confirm() so that a cancel racing
    // with the last BillInserted event doesn't double-dispense change.
    private val stateMutex = Mutex()
    private var sessionState = SessionState.IDLE

    override fun startSession(requiredAmount: Long): Flow<PaymentEvent> {
        this.requiredAmount = requiredAmount
        totalInserted.set(0L)
        sessionState = SessionState.COLLECTING
        return billDriver.billEvents.map { mapBillEvent(it) }
            .onEach { event -> publishBillEvent(event) }
            .onStart { billDriver.connect() }
    }

    private suspend fun mapBillEvent(event: BillEvent): PaymentEvent = stateMutex.withLock {
        if (sessionState == SessionState.CANCELLED || sessionState == SessionState.COMPLETED) {
            return@withLock PaymentEvent.CashRejected("Session closed")
        }
        when (event) {
            is BillEvent.BillInserted -> {
                val running = totalInserted.addAndGet(event.denomination)
                if (running >= requiredAmount) {
                    billDriver.acceptBill()
                    sessionState = SessionState.AUTHORIZED
                    PaymentEvent.CashSufficient(running, running - requiredAmount)
                } else {
                    PaymentEvent.CashInserted(event.denomination, running)
                }
            }
            is BillEvent.Accepted -> PaymentEvent.CashInserted(event.denomination, event.total)
            is BillEvent.Rejected -> PaymentEvent.CashRejected(event.reason)
            BillEvent.Jammed -> PaymentEvent.CashRejected("Bill jammed")
            is BillEvent.ChangeDispensed -> PaymentEvent.ChangeDispensed(event.amount)
        }
    }

    override suspend fun cancel() = stateMutex.withLock {
        if (sessionState == SessionState.CANCELLED || sessionState == SessionState.COMPLETED) return
        val inserted = totalInserted.get()
        val refund = when (sessionState) {
            SessionState.AUTHORIZED -> (inserted - requiredAmount).coerceAtLeast(0L)
            SessionState.COLLECTING,
            SessionState.IDLE,
            -> inserted
            SessionState.CANCELLED,
            SessionState.COMPLETED,
            -> 0L
        }
        billDriver.rejectBill()
        if (refund > 0) billDriver.dispenseChange(refund)
        totalInserted.set(0L)
        sessionState = SessionState.CANCELLED
    }

    override suspend fun confirm() = stateMutex.withLock {
        if (sessionState == SessionState.CANCELLED || sessionState == SessionState.COMPLETED) return
        val change = (totalInserted.get() - requiredAmount).coerceAtLeast(0L)
        if (change > 0) billDriver.dispenseChange(change)
        totalInserted.set(0L)
        sessionState = SessionState.COMPLETED
    }

    private suspend fun publishBillEvent(event: PaymentEvent) {
        when (event) {
            is PaymentEvent.CashInserted -> hardwareEventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "CashPaymentProcessor",
                    eventName = "bill_inserted",
                    amount = event.total,
                )
            )
            is PaymentEvent.CashSufficient -> hardwareEventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "CashPaymentProcessor",
                    eventName = "cash_sufficient",
                    amount = event.total,
                    detail = "change=${event.change}",
                )
            )
            is PaymentEvent.CashRejected -> hardwareEventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "CashPaymentProcessor",
                    eventName = "bill_rejected",
                    detail = event.reason,
                )
            )
            is PaymentEvent.ChangeDispensed -> hardwareEventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "CashPaymentProcessor",
                    eventName = "change_dispensed",
                    amount = event.amount,
                )
            )
            else -> Unit
        }
    }
}
