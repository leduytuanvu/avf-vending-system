package com.avf.vending.hardware.xy.strategy

import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.transport.strategy.SerialStrategy

object XYStrategyDefaults {
    /**
     * Primary port used as the bus-arbiter lock key.
     * WARNING: the fallback chain also includes /dev/ttyS1, which is the bill
     * acceptor's primary port. If the machine ever falls back to ttyS1 while
     * the bill driver is also on ttyS1, set both drivers to the same PORT_ID
     * so HardwareBusArbiter serialises them on that shared bus.
     */
    const val PORT_ID = "/dev/ttyS0"

    /**
     * Default fallback chain: Serial S0 (9600N) → Serial S1 (9600N) → USB-Serial
     */
    fun buildStrategies(): List<CommunicationStrategy> = listOf(
        SerialStrategy(SerialConfig(portName = "/dev/ttyS0", baudRate = 9600)),
        SerialStrategy(SerialConfig(portName = "/dev/ttyS1", baudRate = 9600)),
        // UsbSerialStrategy added at runtime via TransportFactory
    )
}
