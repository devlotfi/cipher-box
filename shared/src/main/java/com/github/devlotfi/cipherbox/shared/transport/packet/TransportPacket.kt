package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TransportPacket : IdentifiedRequest {
  @Serializable
  @SerialName("DATA_TOR")
  data class DataTor(
    val level2CipherPacket: Level2CipherPacket,
    override val requestId: String,
  ) : TransportPacket

  @Serializable
  @SerialName("DATA_MQTT")
  data class DataMqtt(
    val level2CipherPacket: Level2CipherPacket,
    val ackTopic: String,
    override val requestId: String,
  ) : TransportPacket

  @Serializable
  @SerialName("ACK")
  data class Ack(
    override val requestId: String,
  ) : TransportPacket
}