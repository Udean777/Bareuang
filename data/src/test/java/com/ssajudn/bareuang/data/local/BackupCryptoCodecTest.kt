package com.ssajudn.bareuang.data.local

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoCodecTest {
    private val codec = BackupCryptoCodec()

    @Test
    fun `encrypted data round trips and does not expose payload`() {
        val plain = "Bareuang Backup – transaksi Rp 25.000".toByteArray()
        val encrypted = codec.encrypt(plain, "password yang cukup kuat".toCharArray())

        assertTrue(codec.isEncrypted(encrypted))
        assertFalse(encrypted.toString(Charsets.ISO_8859_1).contains("transaksi"))
        assertArrayEquals(plain, codec.decrypt(encrypted, "password yang cukup kuat".toCharArray()))
    }

    @Test
    fun `each export gets a fresh salt and nonce`() {
        val plain = "same content".toByteArray()
        val password = "password yang cukup kuat".toCharArray()

        val first = codec.encrypt(plain, password)
        val second = codec.encrypt(plain, password)

        assertNotEquals(first.toList(), second.toList())
        assertArrayEquals(plain, codec.decrypt(first, password))
        assertArrayEquals(plain, codec.decrypt(second, password))
    }

    @Test
    fun `wrong password and changed ciphertext are rejected`() {
        val encrypted = codec.encrypt("private data".toByteArray(), "password yang cukup kuat".toCharArray())

        assertThrows<InvalidBackupPasswordOrCorruptFileException> {
            codec.decrypt(encrypted, "kata sandi lainnya".toCharArray())
        }
        val changed = encrypted.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 0x01).toByte() }
        assertThrows<InvalidBackupPasswordOrCorruptFileException> {
            codec.decrypt(changed, "password yang cukup kuat".toCharArray())
        }
    }

    @Test
    fun `malformed or truncated envelope is rejected`() {
        assertThrows<InvalidBackupFileException> { codec.decrypt(byteArrayOf(1, 2, 3), "password yang cukup kuat".toCharArray()) }
        val encrypted = codec.encrypt("private data".toByteArray(), "password yang cukup kuat".toCharArray())
        assertThrows<InvalidBackupFileException> { codec.decrypt(encrypted.copyOf(20), "password yang cukup kuat".toCharArray()) }
        val invalidIterations = encrypted.copyOf().also { bytes ->
            bytes[11] = 0x7f
            bytes[12] = 0x7f
            bytes[13] = 0x7f
            bytes[14] = 0x7f
        }
        assertThrows<InvalidBackupFileException> { codec.decrypt(invalidIterations, "password yang cukup kuat".toCharArray()) }
    }

    @Test
    fun `export requires a sufficiently long password`() {
        assertThrows<IllegalArgumentException> { codec.encrypt(byteArrayOf(1), "short".toCharArray()) }
    }

    private inline fun <reified T : Throwable> assertThrows(noinline action: () -> Unit) {
        try {
            action()
            throw AssertionError("Expected ${T::class.java.simpleName}")
        } catch (actual: Throwable) {
            if (actual !is T) throw actual
        }
    }
}
