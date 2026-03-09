package com.avf.vending.hardware.xy

import com.avf.vending.hardware.api.model.SlotStatus

object XYInventoryParser {
    /** Each slot is 3 bytes: [row][col][stock] */
    fun parse(data: ByteArray): List<SlotStatus> {
        val result = mutableListOf<SlotStatus>()
        var i = 0
        while (i + 2 < data.size) {
            val row = data[i].toInt().toChar()
            val col = data[i + 1].toInt() and 0xFF
            val stock = data[i + 2].toInt() and 0xFF
            result.add(SlotStatus("$row$col", stock, motorOk = true))
            i += 3
        }
        return result
    }
}
