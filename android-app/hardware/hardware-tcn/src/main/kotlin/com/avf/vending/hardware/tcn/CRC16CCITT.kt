package com.avf.vending.hardware.tcn

object CRC16CCITT {
    private const val POLYNOMIAL = 0x1021
    private const val INITIAL_VALUE = 0xFFFF

    fun calculate(data: ByteArray): Int {
        var crc = INITIAL_VALUE
        for (b in data) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor POLYNOMIAL else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc
    }

    fun hiLo(crc: Int): Pair<Byte, Byte> =
        Pair(((crc shr 8) and 0xFF).toByte(), (crc and 0xFF).toByte())
}
