package org.example.contact

import kotlinx.serialization.Serializable

@Serializable
data class SequencedEncryptionKeyPair(
    val sequence: Int,
    val publicKey: String,
    val privateKey: String,
)