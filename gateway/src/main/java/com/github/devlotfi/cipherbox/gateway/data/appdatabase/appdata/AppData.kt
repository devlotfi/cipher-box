package com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class AppDataEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val duressPin: String,
  val aesGcmData: AesGcmData,
  val argon2IdData: Argon2IdData
) {
  companion object {
    const val SINGLETON_ID = 0
  }

  @Serializable
  data class AesGcmData(
    val pinIv: String,
    val ketStoreIv: String,
    val ciphertext: String,
  )

  @Serializable
  data class Argon2IdData(
    val salt: String,
  )
}