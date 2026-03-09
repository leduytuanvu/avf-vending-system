package com.avf.vending.hardware.api.event

sealed class MachineEvent {
    data class SlotEmpty(val slotId: String) : MachineEvent()
    data class MotorJammed(val slotId: String, val code: Int) : MachineEvent()
    object DoorOpened : MachineEvent()
    object DoorClosed : MachineEvent()
    data class TemperatureAlert(val celsius: Float) : MachineEvent()
}
