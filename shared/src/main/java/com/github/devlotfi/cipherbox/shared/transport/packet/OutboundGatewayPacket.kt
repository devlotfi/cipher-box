package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface OutboundGatewayPacket : IdentifiedRequest {
  @Serializable
  @SerialName("DATA_TOR")
  data class DataTor(
    val url: String,
    val level2CipherPacket: Level2CipherPacket,
    override val requestId: String,
  ) : OutboundGatewayPacket

  @Serializable
  @SerialName("DATA_MQTT")
  data class DataMqtt(
    val topic: String,
    val level2CipherPacket: Level2CipherPacket,
    override val requestId: String,
  ) : OutboundGatewayPacket

  @Serializable
  @SerialName("ACK")
  data class Ack(
    override val requestId: String,
  ) : OutboundGatewayPacket
}