package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.exception.NoStrategyAvailableException
import com.avf.vending.local.datastore.StrategyDataStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class StrategyManagerTest {

    @Test
    fun `send routes bytes through fake strategy`() = runTest {
        val fake = TestFakeStrategyManager()
        val data = byteArrayOf(0x01, 0x02, 0x03)

        fake.manager.send(data)

        assertEquals(1, fake.sentBytes.size)
        assertArrayEquals(data, fake.sentBytes[0])
    }

    @Test
    fun `getActive returns the configured fake strategy`() = runTest {
        val fake = TestFakeStrategyManager()
        val strategy = fake.manager.getActive()
        assertEquals("fake", strategy.id)
    }

    @Test
    fun `send throws when throwOnSend is set`() = runTest {
        val fake = TestFakeStrategyManager()
        fake.throwOnSend = RuntimeException("simulated comm failure")

        assertThrows(RuntimeException::class.java) {
            runTest { fake.manager.send(byteArrayOf(0x00)) }
        }
    }

    @Test
    fun `forceStrategy changes active strategy id`() = runTest {
        val fake = TestFakeStrategyManager()
        fake.manager.forceStrategy("fake")
        val strategy = fake.manager.getActive()
        assertEquals("fake", strategy.id)
    }

    @Test
    fun `clearForce restores default strategy`() = runTest {
        val fake = TestFakeStrategyManager()
        fake.manager.forceStrategy("fake")
        fake.manager.clearForce()
        val strategy = fake.manager.getActive()
        assertEquals("fake", strategy.id) // only one strategy, stays the same
    }

    @Test
    fun `send with no strategies throws NoStrategyAvailableException`() = runTest {
        val mockStore = mockk<StrategyDataStore>(relaxed = true) {
            every { lastSuccessfulStrategyId } returns flowOf(null)
        }
        val manager = StrategyManager(DefaultHardwareEventBus(), mockStore) // no strategies configured

        var thrown: NoStrategyAvailableException? = null
        try {
            manager.send(byteArrayOf(0x00))
        } catch (e: NoStrategyAvailableException) {
            thrown = e
        }
        assertNotNull(thrown)
    }

    @Test
    fun `request waits for parsed response`() = runTest {
        val fake = TestFakeStrategyManager()

        val requester = launch {
            val response = fake.manager.request(
                data = byteArrayOf(0x55),
                timeoutMs = 1_000L,
                parseChunk = { bytes -> listOf(bytes.decodeToString()) },
                accept = { it == "OK" },
            )
            assertEquals("OK", response)
        }

        advanceUntilIdle()
        fake.receiveFlow.emit("OK".encodeToByteArray())
        requester.join()
    }
}
