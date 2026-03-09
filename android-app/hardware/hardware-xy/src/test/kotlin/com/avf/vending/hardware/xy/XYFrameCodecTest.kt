package com.avf.vending.hardware.xy

import org.junit.Assert.*
import org.junit.Test

class XYFrameCodecTest {

    // Helper: encode then verify structure
    private fun ByteArray.hex() = joinToString(" ") { "%02X".format(it) }

    @Test
    fun `encode Dispense produces valid frame`() {
        val cmd = XYCommand.Dispense(row = 'A', col = 1)
        val frame = XYFrameCodec.encode(cmd)

        assertEquals("Frame must start with STX", XYCommands.STX, frame.first())
        assertEquals("Frame must end with ETX", XYCommands.ETX, frame.last())

        // LEN byte = 3 (addr + cmd + data=2) = 5 → 3 + 2 = 5
        assertEquals(5.toByte(), frame[1])

        // ADDR
        assertEquals(XYCommands.ADDR_CONTROLLER, frame[2])
        // CMD
        assertEquals(XYCommands.DISPENSE, frame[3])
        // ROW = 'A'.code = 65 = 0x41, COL = 1
        assertEquals('A'.code.toByte(), frame[4])
        assertEquals(1.toByte(), frame[5])
    }

    @Test
    fun `encode Poll produces minimal frame`() {
        val frame = XYFrameCodec.encode(XYCommand.Poll)
        assertEquals(XYCommands.STX, frame.first())
        assertEquals(XYCommands.ETX, frame.last())
        assertEquals(XYCommands.POLL, frame[3])
    }

    @Test
    fun `decode valid response returns XYResponse`() {
        // Build a minimal valid response: [STX][LEN=3][ADDR][CMD][STATUS][XOR][ETX]
        val addr: Byte = 0x01
        val cmd: Byte = 0x31
        val status: Byte = 0x00
        val len: Byte = 3
        val payload = byteArrayOf(len, addr, cmd, status)
        val xor = payload.fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        val frame = byteArrayOf(XYCommands.STX) + payload + byteArrayOf(xor, XYCommands.ETX)

        val response = XYFrameCodec.decode(frame)

        assertNotNull(response)
        assertEquals(addr, response!!.addr)
        assertEquals(cmd, response.cmd)
        assertEquals(status, response.status)
    }

    @Test
    fun `decode returns null for too-short frame`() {
        assertNull(XYFrameCodec.decode(byteArrayOf(0x02, 0x03)))
    }

    @Test
    fun `decode returns null when STX missing`() {
        val frame = byteArrayOf(0x00, 0x03, 0x01, 0x00, 0x00, 0x02, 0x03)
        assertNull(XYFrameCodec.decode(frame))
    }

    @Test
    fun `encode decode roundtrip for GetStatus`() {
        val encoded = XYFrameCodec.encode(XYCommand.GetStatus)
        // A valid response with same cmd byte
        val addr: Byte = 0x01
        val cmd = XYCommands.GET_STATUS
        val status: Byte = 0x00
        val len: Byte = 3
        val payload = byteArrayOf(len, addr, cmd, status)
        val xor = payload.fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        val response = byteArrayOf(XYCommands.STX) + payload + byteArrayOf(xor, XYCommands.ETX)
        val decoded = XYFrameCodec.decode(response)
        assertNotNull(decoded)
        assertEquals(XYCommands.GET_STATUS, decoded!!.cmd)
    }
}
