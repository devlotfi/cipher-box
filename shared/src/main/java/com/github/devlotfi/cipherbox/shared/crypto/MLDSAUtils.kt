package org.example.utils

import org.bouncycastle.crypto.generators.MLDSAKeyPairGenerator
import org.bouncycastle.crypto.params.*
import org.bouncycastle.crypto.signers.MLDSASigner
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import kotlin.io.encoding.Base64

object MLDSAUtils {
    private val params = MLDSAParameters.ml_dsa_65
    private val random = SecureRandom()

    data class GenerateKeyPairResult(
        val publicKey: String,
        val privateKey: String
    )

    fun generateKeyPair(): GenerateKeyPairResult {
        val generator = MLDSAKeyPairGenerator()
        generator.init(
            MLDSAKeyGenerationParameters(
                random,
                params
            )
        )
        val keyPair = generator.generateKeyPair()

        val publicKeyBytes = (keyPair.public as MLDSAPublicKeyParameters).encoded
        val privateKeyBytes = (keyPair.private as MLDSAPrivateKeyParameters).encoded

        return GenerateKeyPairResult(
            publicKey = Base64.encode(publicKeyBytes),
            privateKey = Base64.encode(privateKeyBytes)
        )
    }

    // Sign a String
    fun sign(
        data: String,
        privateKey: String
    ): String {
        val messageBytes = data.toByteArray(StandardCharsets.UTF_8)
        val signer = MLDSASigner()

        signer.init(
            true,
            ParametersWithRandom(
                MLDSAPrivateKeyParameters(
                    params,
                    Base64.decode(privateKey)
                ),
                random
            )
        )
        signer.update(
            messageBytes,
            0,
            messageBytes.size
        )
        return Base64.encode(signer.generateSignature())
    }

    // Verify a String
    fun verify(
        data: String,
        signature: String,
        publicKey: String
    ): Boolean {
        try {
            val messageBytes = data.toByteArray(StandardCharsets.UTF_8)
            val signer = MLDSASigner()
            signer.init(
                false,
                MLDSAPublicKeyParameters(
                    params,
                    Base64.decode(publicKey)
                )
            )
            signer.update(
                messageBytes,
                0,
                messageBytes.size
            )
            return signer.verifySignature(Base64.decode(signature))
        } catch (e: Exception) {
            return false
        }
    }
}
