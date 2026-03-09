package com.avf.vending.testing

import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.model.ConnectionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeBillValidatorDriverTest {

    private lateinit var driver: FakeBillValidatorDriver

    @Before
    fun setUp() {
        driver = FakeBillValidatorDriver()
    }

    @Test
    fun `connect updates connectionState to CONNECTED`() = runTest {
        driver.connect()
        assertEquals(ConnectionState.CONNECTED, driver.connectionState.first())
    }

    @Test
    fun `disconnect updates connectionState to DISCONNECTED`() = runTest {
        driver.connect()
        driver.disconnect()
        assertEquals(ConnectionState.DISCONNECTED, driver.connectionState.first())
    }

    @Test
    fun `connect returns connectResult`() = runTest {
        driver.connectResult = false
        assertFalse(driver.connect())
    }

    @Test
    fun `acceptBill sets acceptBillCalled flag`() = runTest {
        driver.acceptBill()
        assertTrue(driver.acceptBillCalled)
    }

    @Test
    fun `rejectBill sets flag and emits Rejected event`() = runTest {
        val events = mutableListOf<BillEvent>()
        val job = launch { driver.billEvents.collect { events.add(it) } }

        driver.rejectBill()

        // Let coroutines process
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertTrue(driver.rejectBillCalled)
        assertTrue(events.any { it is BillEvent.Rejected })
    }

    @Test
    fun `simulateInsert emits BillInserted event`() = runTest {
        val events = mutableListOf<BillEvent>()
        val job = launch { driver.billEvents.collect { events.add(it) } }

        driver.simulateInsert(10_000L)

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
        assertEquals(BillEvent.BillInserted(10_000L), events[0])
    }

    @Test
    fun `simulateJam emits Jammed object event`() = runTest {
        val events = mutableListOf<BillEvent>()
        val job = launch { driver.billEvents.collect { events.add(it) } }

        driver.simulateJam()

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
        assertSame(BillEvent.Jammed, events[0])
    }

    @Test
    fun `simulateAccepted emits Accepted event with correct values`() = runTest {
        val events = mutableListOf<BillEvent>()
        val job = launch { driver.billEvents.collect { events.add(it) } }

        driver.simulateAccepted(denomination = 50_000L, total = 50_000L)

        testScheduler.advanceUntilIdle()
        job.cancel()

        val accepted = events.filterIsInstance<BillEvent.Accepted>()
        assertEquals(1, accepted.size)
        assertEquals(50_000L, accepted[0].denomination)
        assertEquals(50_000L, accepted[0].total)
    }

    @Test
    fun `dispenseChange emits ChangeDispensed event`() = runTest {
        val events = mutableListOf<BillEvent>()
        val job = launch { driver.billEvents.collect { events.add(it) } }

        driver.dispenseChange(5_000L)

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(BillEvent.ChangeDispensed(5_000L), events[0])
    }
}
