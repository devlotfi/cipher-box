package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface InboundGatewayPacket : IdentifiedRequest {
  @Serializable
  @SerialName("DATA")
  data class Data(
    val level2CipherPacket: Level2CipherPacket,
    override val requestId: String,
  ) : InboundGatewayPacket

  @Serializable
  @SerialName("ACK")
  data class Ack(
    val success: Boolean,
    override val requestId: String,
  ) : InboundGatewayPacket
}