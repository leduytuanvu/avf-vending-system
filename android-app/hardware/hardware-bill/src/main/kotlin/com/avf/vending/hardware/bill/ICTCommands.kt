package com.avf.vending.hardware.bill

object ICTCommands {
    const val RESET: Byte = 10
    const val POLL_STATUS: Byte = 25
    const val ESCROW_DECISION: Byte = 26
    const val DISPENSE_CHANGE: Byte = 28
    const val ACCEPT: Byte = 0x01
    const val REJECT: Byte = 0x00
}
