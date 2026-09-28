package com.ssajudn.bareuang.data.local

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

internal class BackupTooLargeException : IOException()

/** Reads no more than [maxBytes] plus one byte from an untrusted document stream. */
internal fun readBackupBytes(input: InputStream, maxBytes: Int): ByteArray {
    require(maxBytes in 0 until Int.MAX_VALUE)

    val output = ByteArrayOutputStream(minOf(maxBytes, 8 * 1024))
    val buffer = ByteArray(minOf(maxBytes.coerceAtLeast(1), 8 * 1024))
    var total = 0

    while (true) {
        val remaining = maxBytes - total
        val requested = minOf(buffer.size, remaining + 1)
        val count = input.read(buffer, 0, requested)

        if (count < 0) return output.toByteArray()
        if (count == 0) {
            val next = input.read()
            if (next < 0) return output.toByteArray()
            if (total == maxBytes) throw BackupTooLargeException()
            output.write(next)
            total++
            continue
        }

        if (count > remaining) throw BackupTooLargeException()
        output.write(buffer, 0, count)
        total += count
    }
}
