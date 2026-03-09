package com.avf.vending.hardware.me

sealed class MeCommand {
    data class Dispense(val row: Char, val col: Int) : MeCommand()
    object Poll : MeCommand()
    object GetInventory : MeCommand()
    object GetStatus : MeCommand()
}
