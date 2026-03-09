package com.avf.vending.hardware.api.model

data class SlotStatus(
    val slotId: String,
    val stock: Int,
    val motorOk: Boolean,
)
