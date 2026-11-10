package org.example.utils

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

import java.security.SecureRandom
import kotlin.io.encoding.Base64

object ARGON2IDUtils {
    private val random = SecureRandom()

    private const val SALT_SIZE = 16
    private const val KEY_SIZE = 32

    private const val ITERATIONS = 10
    private const val MEMORY_KB = 262144
    private const val PARALLELISM = 1

    fun generateSalt(): String {
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)
        return Base64.encode(salt)
    }

    fun deriveKey(
        password: String,
        salt: String
    ): String {

        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val saltBytes = Base64.decode(salt)
        val params = Argon2Parameters.Builder(
            Argon2Parameters.ARGON2_id
        )
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_KB)
            .withParallelism(PARALLELISM)
            .withSalt(saltBytes)
            .build()
        val generator = Argon2BytesGenerator()
        generator.init(params)
        val derivedKey = ByteArray(KEY_SIZE)
        generator.generateBytes(
            passwordBytes,
            derivedKey
        )
        return Base64.encode(derivedKey)
    }
}