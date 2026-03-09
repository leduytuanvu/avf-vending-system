package com.avf.vending.hardware.tcn

data class TCNResponse(
    val seq: Byte,
    val cmdCode: Byte,
    val data: ByteArray,
    val crc: Int,
) {
    val isSuccess get() = data.firstOrNull()?.toInt() == 0x00
}
