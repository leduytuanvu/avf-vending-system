package com.avf.vending.common.extension

import com.avf.vending.domain.model.SlotAddress

fun String.toSlotAddress(): SlotAddress? {
    if (length < 2) return null
    val row = this[0]
    val col = substring(1).toIntOrNull() ?: return null
    return SlotAddress(row, col)
}
