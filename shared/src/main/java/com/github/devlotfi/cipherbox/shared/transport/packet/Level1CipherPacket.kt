package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.Serializable

@Serializable
data class Level1CipherPacket(
  val userId: String,
  val mlDsaData: MLDSAData,
  val signedDataJson: String,
) {
  @Serializable
  data class MLDSAData(
    val signature: String,
  )
}