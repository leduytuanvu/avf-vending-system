package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.config.Rs485Config
import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.config.TcpConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.transport.strategy.*
import javax.inject.Inject

class TransportFactory @Inject constructor(
    private val usbTransport: UsbSerialTransport,
) {
    fun createSerial(config: SerialConfig): CommunicationStrategy = SerialStrategy(config)
    fun createRs485(config: Rs485Config): CommunicationStrategy = Rs485Strategy(config)
    fun createTcp(config: TcpConfig): CommunicationStrategy = TcpStrategy(config)
    fun createUsb(config: SerialConfig): CommunicationStrategy = UsbSerialStrategy(config, usbTransport)
}
