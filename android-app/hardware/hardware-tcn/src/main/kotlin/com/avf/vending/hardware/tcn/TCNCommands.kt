package com.avf.vending.hardware.tcn

object TCNCommands {
    const val VEND_REQUEST: Byte = 0x10
    const val STATUS_POLL: Byte = 0x11
    const val GET_INVENTORY: Byte = 0x12
    const val ACK: Byte = 0x06
    const val NAK: Byte = 0x15
}
