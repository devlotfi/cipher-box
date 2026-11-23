package com.github.devlotfi.cipherbox.shared.transport.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MessageData {
  fun toPayload(basePayload: MessagePayload.BasePayload): MessagePayload

  @Serializable
  @SerialName("TEXT")
  data class Text(
    val text: String,
  ) : MessageData {
    override fun toPayload(basePayload: MessagePayload.BasePayload): MessagePayload =
      MessagePayload.Text(basePayload, this)
  }

  @Serializable
  @SerialName("FILE")
  data class File(
    val extension: String,
    val base64: String,
  ) : MessageData {
    override fun toPayload(basePayload: MessagePayload.BasePayload): MessagePayload =
      MessagePayload.File(basePayload, this)
  }
}