package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.command.HardwareCommand
import com.avf.vending.hardware.api.event.HardwareEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultHardwareCommandQueueTest {
    @Test
    fun `commands on the same bus execute sequentially`() = runTest {
        val queue = DefaultHardwareCommandQueue(HardwareBusArbiter(), DefaultHardwareEventBus(), backgroundScope)
        val order = mutableListOf<String>()

        val first = async {
            queue.submit(HardwareCommand(name = "first", busKey = "shared")) {
                order += "first-start"
                delay(50)
                order += "first-end"
                "first-result"
            }
        }
        val second = async {
            queue.submit(HardwareCommand(name = "second", busKey = "shared")) {
                order += "second-start"
                order += "second-end"
                "second-result"
            }
        }

        assertEquals("first-result", first.await())
        assertEquals("second-result", second.await())
        assertEquals(
            listOf("first-start", "first-end", "second-start", "second-end"),
            order,
        )
    }

    @Test
    fun `submit emits queued started and succeeded events`() = runTest {
        val eventBus = DefaultHardwareEventBus()
        val queue = DefaultHardwareCommandQueue(HardwareBusArbiter(), eventBus, backgroundScope)
        val events = mutableListOf<HardwareEvent>()

        val collector = launch {
            eventBus.events.take(3).toList(events)
        }
        runCurrent()

        queue.submit(HardwareCommand(name = "inventory", busKey = "ttyS0")) { Unit }
        collector.join()

        assertTrue(events[0] is HardwareEvent.CommandQueued)
        assertTrue(events[1] is HardwareEvent.CommandStarted)
        assertTrue(events[2] is HardwareEvent.CommandSucceeded)
    }
}
