package com.avf.vending.payment.impl

import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.testing.FakeBillValidatorDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CashPaymentProcessorTest {
    @Test
    fun `cancel after authorization refunds change only`() = runTest {
        val driver = FakeBillValidatorDriver()
        val processor = CashPaymentProcessor(driver, TestHardwareEventBus())
        val events = mutableListOf<PaymentEvent>()

        val collector = launch {
            processor.startSession(requiredAmount = 5_000L)
                .take(1)
                .toList(events)
        }

        advanceUntilIdle()
        driver.simulateInsert(10_000L)
        advanceUntilIdle()
        processor.cancel()
        advanceUntilIdle()
        collector.cancel()

        assertEquals(listOf(PaymentEvent.CashSufficient(total = 10_000L, change = 5_000L)), events)
        assertEquals(listOf(5_000L), driver.dispensedChanges)
    }

    @Test
    fun `confirm and cancel race does not double dispense change`() = runTest {
        val driver = FakeBillValidatorDriver()
        val processor = CashPaymentProcessor(driver, TestHardwareEventBus())

        val collector = launch {
            processor.startSession(requiredAmount = 5_000L)
                .take(1)
                .collect {}
        }

        advanceUntilIdle()
        driver.simulateInsert(10_000L)
        advanceUntilIdle()

        val confirmJob = launch { processor.confirm() }
        val cancelJob = launch { processor.cancel() }
        confirmJob.join()
        cancelJob.join()
        advanceUntilIdle()
        collector.cancel()

        assertEquals(listOf(5_000L), driver.dispensedChanges)
        assertTrue(driver.dispensedChanges.sum() == 5_000L)
    }

    private class TestHardwareEventBus : HardwareEventBus {
        override val events: Flow<HardwareEvent> = MutableSharedFlow(extraBufferCapacity = 16)

        override suspend fun publish(event: HardwareEvent) = Unit

        override fun tryPublish(event: HardwareEvent): Boolean = true
    }
}
