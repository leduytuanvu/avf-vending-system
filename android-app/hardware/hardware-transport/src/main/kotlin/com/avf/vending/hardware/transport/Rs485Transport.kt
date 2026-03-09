package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.config.Rs485Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Rs485Transport(private val config: Rs485Config) {
    private val busMutex = Mutex()

    fun readStream(): Flow<ByteArray> = flow {
        // RS485 half-duplex: drain → write → lineDelay → read sequence
        // TODO: integrate with platform RS485 port
    }

    suspend fun write(data: ByteArray) = busMutex.withLock {
        // drain → write → wait lineDelay
        delay(config.lineDelayMs)
        // TODO: write to RS485 output stream
    }

    suspend fun open(): Boolean = false // TODO
    fun close() {} // TODO
}
