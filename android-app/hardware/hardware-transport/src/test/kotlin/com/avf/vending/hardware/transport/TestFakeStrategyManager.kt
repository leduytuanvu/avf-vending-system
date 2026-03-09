package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.local.datastore.StrategyDataStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

class TestFakeStrategyManager {
    val sentBytes = mutableListOf<ByteArray>()
    var throwOnSend: Exception? = null
    val receiveFlow = MutableSharedFlow<ByteArray>(extraBufferCapacity = 16)

    private val fakeStrategy = object : CommunicationStrategy {
        override val id = "fake"
        override val type = StrategyType.SERIAL_RS232
        override suspend fun isAvailable() = true
        override suspend fun connect() = true
        override suspend fun disconnect() = Unit
        override suspend fun send(data: ByteArray) {
            throwOnSend?.let { throw it }
            sentBytes += data
        }

        override fun receiveStream(): Flow<ByteArray> = receiveFlow
    }

    private val mockStrategyDataStore = mockk<StrategyDataStore>(relaxed = true) {
        every { lastSuccessfulStrategyId } returns flowOf(null)
    }

    val manager = StrategyManager(DefaultHardwareEventBus(), mockStrategyDataStore).also {
        it.configure(listOf(fakeStrategy))
    }
}
