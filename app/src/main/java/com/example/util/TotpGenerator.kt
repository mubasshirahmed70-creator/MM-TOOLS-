package com.example.util

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * Standard RFC 6238 TOTP (Time-based One-Time Password) Generator.
 * Implements HMAC-SHA1 algorithm with standard Base32 secret decoding.
 * Works 100% locally and offline without external libraries or internet connection.
 */
object TotpGenerator {

    /**
     * Decodes a standard Base32 encoded string into a ByteArray.
     * Tolerates spaces, dashes, and lowercase characters.
     */
    fun decodeBase32(encoded: String): ByteArray {
        val clean = encoded.uppercase().replace(Regex("[^A-Z2-7]"), "")
        if (clean.isEmpty()) return ByteArray(0)

        val out = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (ch in clean) {
            val value = when (ch) {
                in 'A'..'Z' -> ch - 'A'
                in '2'..'7' -> ch - '2' + 26
                else -> continue
            }

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }

        return out.toByteArray()
    }

    /**
     * Generates a 6-digit TOTP code for the given secret key at the specified or current epoch time.
     * Returns null if the secret key is invalid, contains no Base32 characters, or is empty.
     */
    fun generateCurrentCode(secretKey: String, timeMillis: Long = System.currentTimeMillis()): String? {
        val keyBytes = decodeBase32(secretKey)
        if (keyBytes.isEmpty()) return null

        val timeStep = (timeMillis / 1000L) / 30L
        return try {
            generateTotp(keyBytes, timeStep, 6)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns remaining seconds in the current 30-second window (1..30).
     */
    fun getRemainingSeconds(timeMillis: Long = System.currentTimeMillis()): Int {
        val rem = 30 - ((timeMillis / 1000L) % 30).toInt()
        return if (rem == 0) 30 else rem
    }

    private fun generateTotp(key: ByteArray, timeStep: Long, digits: Int): String {
        val data = ByteBuffer.allocate(8).putLong(timeStep).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "RAW"))
        val hash = mac.doFinal(data)

        val offset = (hash[hash.size - 1].toInt() and 0x0F)
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val modulus = 10.0.pow(digits.toDouble()).toInt()
        val otp = binary % modulus
        return String.format("%0${digits}d", otp)
    }
}
