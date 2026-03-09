package com.avf.vending.hardware.tcn

sealed class TCNCommand {
    data class VendRequest(val row: Char, val col: Int) : TCNCommand()
    object StatusPoll : TCNCommand()
    object GetInventory : TCNCommand()
}
