package com.avf.vending.common.extension

fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }

fun ByteArray.xorChecksum(): Byte = fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }

fun ByteArray.crc16(): Int {
    var crc = 0xFFFF
    for (b in this) {
        crc = crc xor (b.toInt() and 0xFF)
        repeat(8) {
            crc = if (crc and 0x0001 != 0) (crc shr 1) xor 0x1021 else crc shr 1
        }
    }
    return crc and 0xFFFF
}
