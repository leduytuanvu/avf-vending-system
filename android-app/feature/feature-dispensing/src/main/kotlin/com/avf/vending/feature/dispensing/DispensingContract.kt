package com.avf.vending.feature.dispensing

import com.avf.vending.hardware.api.model.DispenseResult

data class DispensingState(
    val transactionId: String = "",
    val slotAddress: String = "",
    val isDispensing: Boolean = true,
    val result: DispenseResult? = null,
    val error: String? = null,
)

sealed class DispensingIntent {
    data class Start(val transactionId: String, val slotAddress: String) : DispensingIntent()
    object Confirm : DispensingIntent()
}

sealed class DispensingEffect {
    object NavigateToIdle : DispensingEffect()
}
