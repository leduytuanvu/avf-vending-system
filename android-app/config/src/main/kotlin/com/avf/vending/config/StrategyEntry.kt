package com.avf.vending.config

/**
 * A single communication strategy entry stored in remote config.
 * Drives StrategyManager at runtime without rebuilding the app.
 */
data class StrategyEntry(
    val id: String,
    val type: String,           // SERIAL_RS232 | RS485 | TCP_IP | USB_SERIAL
    val label: String,          // human-readable, e.g. "Serial S0 primary"
    val portName: String = "",  // /dev/ttyS0, /dev/ttyS1, etc.
    val baudRate: Int = 9600,
    val parity: String = "NONE",// NONE | EVEN | ODD
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val tcpHost: String = "",
    val tcpPort: Int = 0,
    val lineDelayMs: Long = 10L,
    val enabled: Boolean = true,
)
