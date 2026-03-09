package com.avf.vending.hardware.api.model

data class BillStatus(
    val ready: Boolean,
    val stackerCount: Int,
    val errorCode: Int = 0,
)
