package com.avf.vending.hardware.tcn

import com.avf.vending.hardware.api.model.DispenseResult

object TCNStatusMapper {
    fun toDispenseResult(response: TCNResponse): DispenseResult =
        if (response.isSuccess) DispenseResult.Success("")
        else DispenseResult.Failed("TCN error: ${response.data.firstOrNull()?.toInt()}", -1)
}
