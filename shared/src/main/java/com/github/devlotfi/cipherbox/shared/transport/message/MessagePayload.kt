package com.github.devlotfi.cipherbox.shared.transport.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.contact.SequencedEncryptionPublicKey

@Serializable
sealed interface MessagePayload {
  val basePayload: BasePayload

  fun toData(): MessageData

  @Serializable
  data class BasePayload(
    val id: String,
    val publicKey: SequencedEncryptionPublicKey,
  )

  @Serializable
  @SerialName("TEXT")
  data class Text(
    override val basePayload: BasePayload,
    val data: MessageData.Text
  ) : MessagePayload {
    override fun toData(): MessageData {
      return data
    }
  }

  @Serializable
  @SerialName("FILE")
  data class File(
    override val basePayload: BasePayload,
    val data: MessageData.File
  ) : MessagePayload {
    override fun toData(): MessageData {
      return data
    }
  }
}