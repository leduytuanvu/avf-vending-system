package com.avf.vending.hardware.tcn.strategy

import com.avf.vending.hardware.api.config.Rs485Config
import com.avf.vending.hardware.api.config.SerialConfig
import com.avf.vending.hardware.api.config.TcpConfig
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.hardware.transport.strategy.Rs485Strategy
import com.avf.vending.hardware.transport.strategy.SerialStrategy
import com.avf.vending.hardware.transport.strategy.TcpStrategy

object TCNStrategyDefaults {
    const val PORT_ID = "/dev/ttyS0"

    /**
     * Default fallback chain: RS485 S0 (19200) → TCP 192.168.1.100:4001 → Serial S1 (9600N backup)
     */
    fun buildStrategies(): List<CommunicationStrategy> = listOf(
        Rs485Strategy(Rs485Config(portName = "/dev/ttyS0", baudRate = 19200)),
        TcpStrategy(TcpConfig(host = "192.168.1.100", port = 4001)),
        SerialStrategy(SerialConfig(portName = "/dev/ttyS1", baudRate = 9600)),
    )
}
