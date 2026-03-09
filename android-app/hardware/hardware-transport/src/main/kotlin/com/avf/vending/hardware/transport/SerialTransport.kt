package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.config.SerialConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SerialTransport(private val config: SerialConfig) {
    fun readStream(): Flow<ByteArray> = flow {
        // RS232/UART read via InputStream polling
        // TODO: integrate with platform serial port API
    }

    suspend fun write(data: ByteArray) {
        // TODO: write to serial port output stream
    }

    suspend fun open(): Boolean = false // TODO

    fun close() {} // TODO
}
