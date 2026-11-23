package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.Serializable

@Serializable
data class Level2CipherPacket(
  val mlKemData: MLKEMData,
  val hkdfData: HKDFData,
  val aesGcmData: AESGCMData
) {
  @Serializable
  data class MLKEMData(
    val encapsulation: String,
  )

  @Serializable
  data class HKDFData(
    val salt: String,
    val info: String
  )

  @Serializable
  data class AESGCMData(
    val ciphertext: String,
    val iv: String,
  )
}