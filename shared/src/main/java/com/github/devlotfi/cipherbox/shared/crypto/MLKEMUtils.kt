package org.example.utils

import org.bouncycastle.crypto.generators.MLKEMKeyPairGenerator
import org.bouncycastle.crypto.kems.MLKEMExtractor
import org.bouncycastle.crypto.kems.MLKEMGenerator
import org.bouncycastle.crypto.params.MLKEMKeyGenerationParameters
import org.bouncycastle.crypto.params.MLKEMParameters
import org.bouncycastle.crypto.params.MLKEMPrivateKeyParameters
import org.bouncycastle.crypto.params.MLKEMPublicKeyParameters
import java.security.SecureRandom
import kotlin.io.encoding.Base64

object MLKEMUtils {
    private val params = MLKEMParameters.ml_kem_768
    private val random = SecureRandom()

    data class GenerateKeyPairResult(
        val publicKey: String,
        val privateKey: String
    )

    // Generate ML-KEM key pair
    fun generateKeyPair(): GenerateKeyPairResult {
        val generator = MLKEMKeyPairGenerator()
        generator.init(
            MLKEMKeyGenerationParameters(
                random,
                params
            )
        )

        val keyPair = generator.generateKeyPair()
        val publicKey =
            (keyPair.public as MLKEMPublicKeyParameters).encoded
        val privateKey =
            (keyPair.private as MLKEMPrivateKeyParameters).encoded
        return GenerateKeyPairResult(
            publicKey = Base64.encode(publicKey),
            privateKey = Base64.encode(privateKey)
        )
    }

    data class EncapsulateResult(
        val encapsulation: String,
        val sharedSecret: String
    )

    // Encapsulate using recipient's public key
    fun encapsulate(
        publicKey: String
    ): EncapsulateResult {
        val generator = MLKEMGenerator(random)
        val encapsulated = generator.generateEncapsulated(
            MLKEMPublicKeyParameters(
                params,
                Base64.decode(publicKey)
            )
        )
        try {
            return EncapsulateResult(
                encapsulation = Base64.encode(encapsulated.encapsulation),
                sharedSecret = Base64.encode(encapsulated.secret)
            )
        } finally {
            encapsulated.destroy()
        }
    }

    // Decapsulate using recipient's private key
    fun decapsulate(
        encapsulation: String,
        privateKey: String
    ): String {
        val extractor = MLKEMExtractor(
            MLKEMPrivateKeyParameters(
                params,
                Base64.decode(privateKey)
            )
        )
        val sharedSecret = extractor.extractSecret(
            Base64.decode(encapsulation)
        )
        return Base64.encode(sharedSecret)
    }
}