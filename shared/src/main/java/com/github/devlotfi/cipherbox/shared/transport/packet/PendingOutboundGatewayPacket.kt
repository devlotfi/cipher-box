package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.Serializable

@Serializable
data class PendingOutboundGatewayPacket(
  override val requestId: String,
  val level2CipherPacket: Level2CipherPacket,
  val level1CipherPacket: Level1CipherPacket,
  val messageEntity: String,
) : IdentifiedRequest