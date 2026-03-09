package com.avf.vending.hardware.transport.strategy

import com.avf.vending.hardware.api.config.Rs485Config
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.hardware.transport.Rs485Transport
import kotlinx.coroutines.flow.Flow

class Rs485Strategy(private val config: Rs485Config) : CommunicationStrategy {
    private val transport = Rs485Transport(config)
    override val id = "rs485-${config.portName}"
    override val type = StrategyType.RS485

    override suspend fun isAvailable() = true
    override suspend fun connect() = transport.open()
    override suspend fun disconnect() = transport.close()
    override suspend fun send(data: ByteArray) = transport.write(data)
    override fun receiveStream(): Flow<ByteArray> = transport.readStream()
}
