package com.avf.vending.feature.payment

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentStateMachineTest {

    private lateinit var fsm: PaymentStateMachine

    @Before
    fun setUp() {
        fsm = PaymentStateMachine()
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(PaymentFsmState.IDLE, fsm.current)
    }

    @Test
    fun `start transitions IDLE to SELECTING_METHOD`() {
        assertTrue(fsm.start())
        assertEquals(PaymentFsmState.SELECTING_METHOD, fsm.current)
    }

    @Test
    fun `start returns false when not in IDLE`() {
        fsm.start()
        assertFalse(fsm.start())
        assertEquals(PaymentFsmState.SELECTING_METHOD, fsm.current)
    }

    @Test
    fun `methodSelected transitions SELECTING_METHOD to AWAITING_PAYMENT`() {
        fsm.start()
        assertTrue(fsm.methodSelected())
        assertEquals(PaymentFsmState.AWAITING_PAYMENT, fsm.current)
    }

    @Test
    fun `fundsReceived transitions AWAITING_PAYMENT to PROCESSING`() {
        fsm.start(); fsm.methodSelected()
        assertTrue(fsm.fundsReceived())
        assertEquals(PaymentFsmState.PROCESSING, fsm.current)
    }

    @Test
    fun `complete transitions PROCESSING to COMPLETED`() {
        fsm.start(); fsm.methodSelected(); fsm.fundsReceived()
        assertTrue(fsm.complete())
        assertEquals(PaymentFsmState.COMPLETED, fsm.current)
    }

    @Test
    fun `fail transitions AWAITING_PAYMENT to FAILED`() {
        fsm.start(); fsm.methodSelected()
        assertTrue(fsm.fail())
        assertEquals(PaymentFsmState.FAILED, fsm.current)
    }

    @Test
    fun `fail transitions PROCESSING to FAILED`() {
        fsm.start(); fsm.methodSelected(); fsm.fundsReceived()
        assertTrue(fsm.fail())
        assertEquals(PaymentFsmState.FAILED, fsm.current)
    }

    @Test
    fun `fail returns false from IDLE`() {
        assertFalse(fsm.fail())
        assertEquals(PaymentFsmState.IDLE, fsm.current)
    }

    @Test
    fun `cancel transitions SELECTING_METHOD to CANCELLED`() {
        fsm.start()
        assertTrue(fsm.cancel())
        assertEquals(PaymentFsmState.CANCELLED, fsm.current)
    }

    @Test
    fun `cancel transitions AWAITING_PAYMENT to CANCELLED`() {
        fsm.start(); fsm.methodSelected()
        assertTrue(fsm.cancel())
        assertEquals(PaymentFsmState.CANCELLED, fsm.current)
    }

    @Test
    fun `cancel returns false from PROCESSING`() {
        fsm.start(); fsm.methodSelected(); fsm.fundsReceived()
        assertFalse(fsm.cancel())
        assertEquals(PaymentFsmState.PROCESSING, fsm.current)
    }

    @Test
    fun `startRefund transitions FAILED to REFUNDING`() {
        fsm.start(); fsm.methodSelected(); fsm.fail()
        assertTrue(fsm.startRefund())
        assertEquals(PaymentFsmState.REFUNDING, fsm.current)
    }

    @Test
    fun `refundComplete transitions REFUNDING to CANCELLED`() {
        fsm.start(); fsm.methodSelected(); fsm.fail(); fsm.startRefund()
        assertTrue(fsm.refundComplete())
        assertEquals(PaymentFsmState.CANCELLED, fsm.current)
    }

    @Test
    fun `reset always returns to IDLE`() {
        fsm.start(); fsm.methodSelected(); fsm.fundsReceived()
        fsm.reset()
        assertEquals(PaymentFsmState.IDLE, fsm.current)
    }

    @Test
    fun `full happy path transitions correctly`() {
        val states = mutableListOf<PaymentFsmState>()
        fsm.state.value.also { states.add(it) }
        fsm.start(); states.add(fsm.current)
        fsm.methodSelected(); states.add(fsm.current)
        fsm.fundsReceived(); states.add(fsm.current)
        fsm.complete(); states.add(fsm.current)

        assertEquals(listOf(
            PaymentFsmState.IDLE,
            PaymentFsmState.SELECTING_METHOD,
            PaymentFsmState.AWAITING_PAYMENT,
            PaymentFsmState.PROCESSING,
            PaymentFsmState.COMPLETED,
        ), states)
    }
}
