package com.avf.vending.hardware.bill

import com.avf.vending.hardware.api.codec.FrameCodec

/**
 * ICT-BC Frame: [DA][LNG][SA][RC][CMD][DATA...][FCC]
 * FCC = DA XOR LNG XOR SA XOR RC XOR CMD XOR all(DATA)
 * NOTE: serial port MUST use 9600/8E1 (Even parity)
 */
data class ICTFrame(val sa: Byte, val cmd: Byte, val data: ByteArray)

object ICTFrameCodec : FrameCodec<ICTFrame, ICTFrame> {

    override fun encode(command: ICTFrame): ByteArray {
        val da = ICTAddresses.BILL
        val sa = command.sa
        val rc = ICTAddresses.RESERVE
        val cmd = command.cmd
        val data = command.data
        val lng = (4 + data.size).toByte()
        val fcc = byteArrayOf(da, lng, sa, rc, cmd).let { header ->
            (header + data).fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        }
        return byteArrayOf(da, lng, sa, rc, cmd) + data + byteArrayOf(fcc)
    }

    override fun decode(bytes: ByteArray): ICTFrame? {
        if (bytes.size < 6) return null
        val da = bytes[0]
        val lng = bytes[1].toInt() and 0xFF
        if (bytes.size < lng + 2) return null
        val sa = bytes[2]
        val rc = bytes[3]
        val cmd = bytes[4]
        val data = bytes.copyOfRange(5, 2 + lng - 1)
        val fccReceived = bytes[1 + lng]
        val fccCalc = bytes.copyOfRange(0, 1 + lng).fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        if (fccReceived != fccCalc) return null // checksum mismatch
        return ICTFrame(sa, cmd, data)
    }
}
