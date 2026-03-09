package com.avf.vending.common.extension

fun Long.toVnd(): String = "%,d VND".format(this)

fun Int.toBytes(): ByteArray = byteArrayOf(
    ((this shr 8) and 0xFF).toByte(),
    (this and 0xFF).toByte()
)

fun ByteArray.toInt16(): Int = ((this[0].toInt() and 0xFF) shl 8) or (this[1].toInt() and 0xFF)
