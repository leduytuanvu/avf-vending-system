package com.avf.vending.domain.model

data class Slot(
    val id: String,
    val address: SlotAddress,
    val productId: String?,
    val stock: Int,
    val capacity: Int,
)

data class SlotAddress(val row: Char, val col: Int) {
    override fun toString() = "$row$col"
}
