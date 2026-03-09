package com.avf.vending.hardware.co

data class CoResponse(
    val cmdCode: Byte,
    val status: Byte,
    val data: ByteArray,
) {
    val isSuccess get() = status == 0x00.toByte()
}
