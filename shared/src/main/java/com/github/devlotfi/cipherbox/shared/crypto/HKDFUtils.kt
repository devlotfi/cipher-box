package org.example.utils

import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import kotlin.io.encoding.Base64

object HKDFUtils {
    private const val KEY_SIZE = 32
    private const val SALT_SIZE = 32

    fun generateSalt(): String {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return Base64.encode(salt)
    }

    fun deriveKey(
        inputKeyMaterial: String,
        salt: String,
        info: String
    ): String {
        val inputKeyBytes = Base64.decode(inputKeyMaterial)
        val saltBytes = Base64.decode(salt)
        val infoBytes = info.toByteArray(StandardCharsets.UTF_8)

        val hkdf = HKDFBytesGenerator(SHA512Digest())

        hkdf.init(
            HKDFParameters(
                inputKeyBytes,
                saltBytes,
                infoBytes
            )
        )

        val derivedKey = ByteArray(KEY_SIZE)
        hkdf.generateBytes(derivedKey, 0, derivedKey.size)

        return Base64.encode(derivedKey)
    }
}