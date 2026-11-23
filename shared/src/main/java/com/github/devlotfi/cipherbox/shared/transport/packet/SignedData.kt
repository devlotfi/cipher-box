package com.github.devlotfi.cipherbox.shared.transport.packet

import kotlinx.serialization.Serializable

@Serializable
data class SignedData(
  val mlKemData: MLKEMData,
  val hkdfData: HKDFData,
  val aesGcmData: AESGCMData,
) {

  @Serializable
  data class MLKEMData(
    val sequence: Int,
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