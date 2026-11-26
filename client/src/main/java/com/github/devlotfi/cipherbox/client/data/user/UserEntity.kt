package com.github.devlotfi.cipherbox.client.data.user

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val id: String,
  val onionAddress: String,
  val mqttTopic: String,
  val signaturePublicKey: String,
  val signaturePrivateKey: String,
  val encryptionPublicKey: String,
  val encryptionPrivateKey: String,
) {
  companion object {
    const val SINGLETON_ID = 0
  }
}