package com.avf.vending.hardware.transport.strategy

import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.hardware.transport.UsbSerialTransport
import kotlinx.coroutines.flow.Flow

class UsbSerialStrategy(
    private val config: SerialConfig,
    private val transport: UsbSerialTransport,
) : CommunicationStrategy {
    override val id = "usb-serial"
    override val type = StrategyType.USB_SERIAL

    override suspend fun isAvailable() = true
    override suspend fun connect() = transport.open(config)
    override suspend fun disconnect() = transport.close()
    override suspend fun send(data: ByteArray) = transport.write(data)
    override fun receiveStream(): Flow<ByteArray> = transport.readStream()
}
