package com.avf.vending.hardware.xy

import com.avf.vending.hardware.api.model.DispenseResult

object XYStatusMapper {
    fun toDispenseResult(response: XYResponse): DispenseResult = when (response.status.toInt() and 0xFF) {
        0x00 -> DispenseResult.Success("${response.data.getOrElse(0) { 0 }.toInt().toChar()}${response.data.getOrElse(1) { 0 }}")
        0x01 -> DispenseResult.Failed("Motor failure", 0x01)
        0x02 -> DispenseResult.Failed("Slot empty", 0x02)
        0x03 -> DispenseResult.Failed("No product", 0x03)
        else -> DispenseResult.Failed("Unknown error ${response.status}", response.status.toInt())
    }
}
