package org.example.contact

import kotlinx.serialization.Serializable

@Serializable
data class SequencedEncryptionPublicKey(
    val sequence: Int,
    val key: String,
)