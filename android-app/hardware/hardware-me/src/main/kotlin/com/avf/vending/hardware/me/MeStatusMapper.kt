package com.avf.vending.hardware.me

import com.avf.vending.hardware.api.model.DispenseResult

object MeStatusMapper {
    fun toDispenseResult(response: MeResponse): DispenseResult =
        if (response.isSuccess) DispenseResult.Success("")
        else DispenseResult.Failed("ME error: 0x${response.status.toInt().and(0xFF).toString(16)}", -1)
}
