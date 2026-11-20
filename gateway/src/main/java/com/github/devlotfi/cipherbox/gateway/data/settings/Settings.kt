package com.github.devlotfi.cipherbox.gateway.data.settings

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SettingsEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val torSocksProxyPort: Int = 9050,
  val onionAddress: String? = null,
  val torRc: String? = null,
  val serverPort: Int = 8080,
  val apiKey: String? = null,
  val apiKeyHash: String? = null,
  val bluetoothClientMac: String = "AA:BB:CC:DD:EE:01",
  val mqttHost: String? = null,
  val mqttPort: Int? = null,
  val mqttUsername: String? = null,
  val mqttPassword: String? = null,
  val mqttTopic: String? = null,
) {
  fun isMissingSettings(): Boolean =
    apiKey == null ||
      apiKeyHash == null ||
      mqttHost == null ||
      mqttPort == null ||
      mqttTopic == null

  companion object {
    const val SINGLETON_ID = 0
  }
}