package com.avf.vending.hardware.bill

import com.avf.vending.hardware.api.event.BillEvent

object ICTEventMapper {
    fun mapPollResponse(frame: ICTFrame): BillEvent? {
        val statusByte = frame.data.firstOrNull()?.toInt() ?: return null
        val denomination = frame.data.getOrNull(1)?.toLong()?.and(0xFF) ?: 0L
        return when {
            statusByte and 0x10 != 0 -> BillEvent.Jammed
            statusByte and 0x08 != 0 -> BillEvent.BillInserted(denomination = denomination)
            statusByte and 0x04 != 0 -> BillEvent.Rejected("Rejected by validator")
            else -> null
        }
    }
}
