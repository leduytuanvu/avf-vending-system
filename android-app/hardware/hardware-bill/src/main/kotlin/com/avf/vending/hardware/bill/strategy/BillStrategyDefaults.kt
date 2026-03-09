package com.avf.vending.hardware.bill.strategy

import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.transport.strategy.SerialStrategy

object BillStrategyDefaults {
    /**
     * Primary port used as the bus-arbiter lock key.
     * If this matches XYStrategyDefaults.PORT_ID, update XYDriver.portId to
     * this value so both drivers share the same HardwareBusArbiter lock.
     */
    const val PORT_ID = "/dev/ttyS1"

    /**
     * ICT-BC requires EVEN parity (8E1) — using NONE parity will corrupt frames.
     * Fallback chain: Serial S1 (9600/8E1) → USB-Serial (9600/8E1)
     */
    fun buildStrategies(): List<CommunicationStrategy> = listOf(
        SerialStrategy(SerialConfig(
            portName = "/dev/ttyS1",
            baudRate = 9600,
            parity = SerialConfig.Parity.EVEN,
        )),
        // UsbSerialStrategy with parity=EVEN added at runtime
    )
}
