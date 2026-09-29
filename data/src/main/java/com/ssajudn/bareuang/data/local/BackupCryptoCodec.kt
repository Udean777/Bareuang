package com.ssajudn.bareuang.data.local

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class BackupCryptoCodec(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun isEncrypted(bytes: ByteArray): Boolean = bytes.size >= MAGIC.size &&
        MessageDigest.isEqual(MAGIC, bytes.copyOfRange(0, MAGIC.size))

    fun encrypt(plainText: ByteArray, password: CharArray): ByteArray {
        require(password.size >= MIN_PASSWORD_LENGTH)
        require(plainText.isNotEmpty())
        require(plainText.size <= MAX_PLAINTEXT_BYTES)

        val salt = ByteArray(SALT_SIZE).also(secureRandom::nextBytes)
        val nonce = ByteArray(NONCE_SIZE).also(secureRandom::nextBytes)
        val header = ByteBuffer.allocate(HEADER_SIZE)
            .put(MAGIC)
            .putInt(FORMAT_VERSION)
            .putInt(PBKDF2_ITERATIONS)
            .put(salt)
            .put(nonce)
            .array()
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(header)
        val encrypted = cipher.doFinal(plainText)
        return ByteArrayOutputStream(header.size + encrypted.size).apply {
            write(header)
            write(encrypted)
        }.toByteArray()
    }

    fun decrypt(fileBytes: ByteArray, password: CharArray): ByteArray {
        if (!isEncrypted(fileBytes) || fileBytes.size < HEADER_SIZE + TAG_BYTES || fileBytes.size > MAX_ENCRYPTED_BYTES) {
            throw InvalidBackupFileException()
        }
        val input = ByteBuffer.wrap(fileBytes)
        val magic = ByteArray(MAGIC.size).also(input::get)
        val version = input.int
        val iterations = input.int
        if (!MessageDigest.isEqual(MAGIC, magic) || version != FORMAT_VERSION || iterations !in MIN_PBKDF2_ITERATIONS..MAX_PBKDF2_ITERATIONS) {
            throw InvalidBackupFileException()
        }
        val salt = ByteArray(SALT_SIZE).also(input::get)
        val nonce = ByteArray(NONCE_SIZE).also(input::get)
        val header = fileBytes.copyOfRange(0, HEADER_SIZE)
        val cipherText = fileBytes.copyOfRange(HEADER_SIZE, fileBytes.size)
        try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt, iterations), GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(header)
            val plainText = cipher.doFinal(cipherText)
            if (plainText.size > MAX_PLAINTEXT_BYTES) {
                plainText.fill(0)
                throw InvalidBackupFileException()
            }
            return plainText
        } catch (_: AEADBadTagException) {
            throw InvalidBackupPasswordOrCorruptFileException()
        } catch (e: InvalidBackupFileException) {
            throw e
        } catch (e: Exception) {
            throw InvalidBackupPasswordOrCorruptFileException(e)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int = PBKDF2_ITERATIONS): SecretKeySpec {
        if (password.isEmpty()) throw InvalidBackupPasswordOrCorruptFileException()
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            val keyBytes = SecretKeyFactory.getInstance(KDF_ALGORITHM).generateSecret(spec).encoded
            try {
                SecretKeySpec(keyBytes, "AES")
            } finally {
                keyBytes.fill(0)
            }
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 12
        const val MAX_PLAINTEXT_BYTES = 5 * 1024 * 1024
        const val MAX_ENCRYPTED_BYTES = MAX_PLAINTEXT_BYTES + 128
        const val FORMAT_VERSION = 3
        const val PBKDF2_ITERATIONS = 210_000
        private const val MIN_PBKDF2_ITERATIONS = 100_000
        private const val MAX_PBKDF2_ITERATIONS = 500_000
        private const val KEY_BITS = 256
        private const val TAG_BITS = 128
        private const val TAG_BYTES = TAG_BITS / 8
        private const val SALT_SIZE = 16
        private const val NONCE_SIZE = 12
        private const val HEADER_SIZE = 8 + 4 + 4 + SALT_SIZE + NONCE_SIZE
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        private val MAGIC = "BAREUANG".toByteArray(StandardCharsets.US_ASCII)
    }
}

internal class InvalidBackupFileException(cause: Throwable? = null) : Exception(cause)
internal class InvalidBackupPasswordOrCorruptFileException(cause: Throwable? = null) : Exception(cause)
