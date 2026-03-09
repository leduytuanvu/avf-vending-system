package com.avf.vending.hardware.xy

object XYCommands {
    const val DISPENSE: Byte = 0x31
    const val POLL: Byte = 0x00
    const val GET_INVENTORY: Byte = 0x32
    const val GET_STATUS: Byte = 0x01

    const val STX: Byte = 0x02
    const val ETX: Byte = 0x03
    const val ADDR_CONTROLLER: Byte = 0x01
}
