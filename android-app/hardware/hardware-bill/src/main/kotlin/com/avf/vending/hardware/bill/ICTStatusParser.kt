package com.avf.vending.hardware.bill

import com.avf.vending.hardware.api.model.BillStatus

object ICTStatusParser {
    fun parse(frame: ICTFrame): BillStatus {
        val ready = frame.data.getOrElse(0) { 0 }.toInt() and 0x01 != 0
        val stackerCount = frame.data.getOrElse(1) { 0 }.toInt() and 0xFF
        val errorCode = frame.data.getOrElse(2) { 0 }.toInt() and 0xFF
        return BillStatus(ready, stackerCount, errorCode)
    }
}
