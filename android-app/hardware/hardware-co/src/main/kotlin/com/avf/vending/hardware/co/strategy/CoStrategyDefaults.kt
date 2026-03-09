package com.avf.vending.hardware.co.strategy

import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.transport.strategy.SerialStrategy

object CoStrategyDefaults {
    const val PORT_ID = "/dev/ttyS0"

    /**
     * Default fallback chain: Serial S0 (9600N) → Serial S2 (9600N) → USB-Serial adapter
     */
    fun buildStrategies(): List<CommunicationStrategy> = listOf(
        SerialStrategy(SerialConfig(portName = "/dev/ttyS0", baudRate = 9600)),
        SerialStrategy(SerialConfig(portName = "/dev/ttyS2", baudRate = 9600)),
        // UsbSerialStrategy added at runtime via TransportFactory
    )
}
