package com.avf.vending.hardware.xy

data class XYResponse(
    val addr: Byte,
    val cmdCode: Byte,
    val status: Byte,
    val data: ByteArray,
) {
    val isSuccess get() = status == 0x00.toByte()
}
