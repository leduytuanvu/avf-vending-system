package com.avf.vending.hardware.api.config

data class SerialConfig(
    val portName: String,
    val baudRate: Int = 9600,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: Parity = Parity.NONE,
    val timeoutMs: Int = 1000,
) {
    enum class Parity { NONE, EVEN, ODD }
}
