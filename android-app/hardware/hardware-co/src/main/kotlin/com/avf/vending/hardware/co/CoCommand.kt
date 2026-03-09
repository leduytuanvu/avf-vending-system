package com.avf.vending.hardware.co

sealed class CoCommand {
    data class Dispense(val row: Char, val col: Int) : CoCommand()
    object Poll : CoCommand()
    object GetInventory : CoCommand()
    object GetStatus : CoCommand()
}
