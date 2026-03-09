package com.avf.vending.hardware.transport.strategy

import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.hardware.transport.SerialTransport
import kotlinx.coroutines.flow.Flow

class SerialStrategy(private val config: SerialConfig) : CommunicationStrategy {
    private val transport = SerialTransport(config)
    override val id = "serial-${config.portName}"
    override val type = StrategyType.SERIAL_RS232

    override suspend fun isAvailable() = true // TODO: check port availability
    override suspend fun connect() = transport.open()
    override suspend fun disconnect() = transport.close()
    override suspend fun send(data: ByteArray) = transport.write(data)
    override fun receiveStream(): Flow<ByteArray> = transport.readStream()
}
