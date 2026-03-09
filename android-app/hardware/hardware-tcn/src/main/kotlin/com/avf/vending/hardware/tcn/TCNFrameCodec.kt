package com.avf.vending.hardware.tcn

import com.avf.vending.hardware.api.codec.FrameCodec

/**
 * TCN Frame: [0xAA][0x55][LEN_HI][LEN_LO][SEQ][CMD][DATA...][CRC_HI][CRC_LO]
 */
object TCNFrameCodec : FrameCodec<TCNCommand, TCNResponse> {
    private var seqCounter: Byte = 0

    override fun encode(command: TCNCommand): ByteArray {
        val seq = seqCounter++
        val (cmd, data) = when (command) {
            is TCNCommand.VendRequest -> Pair(
                TCNCommands.VEND_REQUEST,
                byteArrayOf(command.row.code.toByte(), command.col.toByte())
            )
            TCNCommand.StatusPoll -> Pair(TCNCommands.STATUS_POLL, byteArrayOf())
            TCNCommand.GetInventory -> Pair(TCNCommands.GET_INVENTORY, byteArrayOf())
        }
        val payload = byteArrayOf(seq, cmd) + data
        val len = payload.size
        val lenBytes = byteArrayOf(((len shr 8) and 0xFF).toByte(), (len and 0xFF).toByte())
        val crc = CRC16CCITT.calculate(payload)
        val (crcHi, crcLo) = CRC16CCITT.hiLo(crc)
        return byteArrayOf(0xAA.toByte(), 0x55) + lenBytes + payload + byteArrayOf(crcHi, crcLo)
    }

    override fun decode(bytes: ByteArray): TCNResponse? {
        if (bytes.size < 8) return null
        if (bytes[0] != 0xAA.toByte() || bytes[1] != 0x55.toByte()) return null
        val len = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        if (bytes.size < 4 + len + 2) return null
        val seq = bytes[4]
        val cmd = bytes[5]
        val data = bytes.copyOfRange(6, 4 + len)
        val crc = ((bytes[4 + len].toInt() and 0xFF) shl 8) or (bytes[4 + len + 1].toInt() and 0xFF)
        return TCNResponse(seq, cmd, data, crc)
    }
}
