package com.avf.vending.hardware.xy

sealed class XYCommand {
    data class Dispense(val row: Char, val col: Int) : XYCommand()
    object Poll : XYCommand()
    object GetInventory : XYCommand()
    object GetStatus : XYCommand()
}
