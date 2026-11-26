package com.github.devlotfi.cipherbox.client.data.contact

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import org.example.contact.SequencedEncryptionKeyPair
import org.example.contact.SequencedEncryptionPublicKey
import java.util.UUID

@Entity
@Serializable
data class ContactEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val username: String,
  val latestMessageTimestamp: Long? = null,
  val onionAddress: String,
  val mqttTopic: String,
  val inboundSignaturePublicKey: String,
  val outboundEncryptionPublicKey: String,
  val outboundSequencedEncryptionPublicKey: SequencedEncryptionPublicKey,
  val currentInboundSequencedEncryptionKeyPair: SequencedEncryptionKeyPair,
  val nextInboundSequencedEncryptionKeyPair: SequencedEncryptionKeyPair,
)