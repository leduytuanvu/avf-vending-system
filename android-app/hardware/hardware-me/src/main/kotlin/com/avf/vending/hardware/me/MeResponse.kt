package com.avf.vending.hardware.me

data class MeResponse(
    val cmdCode: Byte,
    val status: Byte,
    val data: ByteArray,
) {
    val isSuccess get() = status == 0x00.toByte()
}
