package com.avf.vending.hardware.transport.strategy

import com.avf.vending.hardware.api.config.TcpConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.hardware.transport.TcpTransport
import kotlinx.coroutines.flow.Flow

class TcpStrategy(private val config: TcpConfig) : CommunicationStrategy {
    private val transport = TcpTransport(config)
    override val id = "tcp-${config.host}:${config.port}"
    override val type = StrategyType.TCP_IP

    override suspend fun isAvailable() = true
    override suspend fun connect() = transport.connect()
    override suspend fun disconnect() = transport.close()
    override suspend fun send(data: ByteArray) {
        transport.write(data)
    }
    override fun receiveStream(): Flow<ByteArray> = transport.readStream()
}
