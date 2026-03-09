package com.avf.vending.hardware.api.event

sealed class BillEvent {
    data class BillInserted(val denomination: Long) : BillEvent()
    data class Accepted(val denomination: Long, val total: Long) : BillEvent()
    data class Rejected(val reason: String) : BillEvent()
    object Jammed : BillEvent()
    data class ChangeDispensed(val amount: Long) : BillEvent()
}
