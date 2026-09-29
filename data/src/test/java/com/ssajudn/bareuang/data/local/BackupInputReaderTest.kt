package com.ssajudn.bareuang.data.local

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.Assert.assertThrows

class BackupInputReaderTest {
    @Test
    fun acceptsInputExactlyAtLimit() {
        val expected = ByteArray(32) { it.toByte() }

        assertArrayEquals(expected, readBackupBytes(ByteArrayInputStream(expected), expected.size))
    }

    @Test
    fun rejectsInputOneByteOverLimit() {
        assertThrows(BackupTooLargeException::class.java) {
            readBackupBytes(ByteArrayInputStream(ByteArray(33)), 32)
        }
    }

    @Test
    fun stopsReadingAfterOnlyOneByteOverLimit() {
        val limit = 64
        val input = CountingInputStream(ByteArray(limit + 100) { it.toByte() })

        assertThrows(BackupTooLargeException::class.java) {
            readBackupBytes(input, limit)
        }
        org.junit.Assert.assertEquals(limit + 1, input.bytesRead)
    }

    private class CountingInputStream(private val bytes: ByteArray) : InputStream() {
        var bytesRead: Int = 0
            private set

        override fun read(): Int {
            if (bytesRead == bytes.size) return -1
            return bytes[bytesRead++].toInt() and 0xff
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (bytesRead == bytes.size) return -1
            val count = minOf(length, bytes.size - bytesRead)
            bytes.copyInto(buffer, offset, bytesRead, bytesRead + count)
            bytesRead += count
            return count
        }
    }
}
