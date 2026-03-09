package com.avf.vending.hardware.co

import com.avf.vending.hardware.api.model.DispenseResult

object CoStatusMapper {
    fun toDispenseResult(response: CoResponse): DispenseResult =
        if (response.isSuccess) DispenseResult.Success("")
        else DispenseResult.Failed("CO error: 0x${response.status.toInt().and(0xFF).toString(16)}", -1)
}
