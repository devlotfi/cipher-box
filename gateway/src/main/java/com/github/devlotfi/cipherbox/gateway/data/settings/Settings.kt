package com.github.devlotfi.cipherbox.gateway.data.settings

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SettingsEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val onionKey: String? = null,
  val torRc: String? = null,
  val bluetoothMac: String = "AA:BB:CC:DD:EE:01",
  val mqttHost: String? = null,
  val mqttPort: Int = 8883,
  val mqttUsername: String? = null,
  val mqttPassword: String? = null,
  val mqttTopic: String? = null,
) {
  fun isMissingSettings(): Boolean =
    mqttHost == null ||
      mqttTopic == null

  fun toIntentExtras(): GatewayForegroundService.IntentExtras =
    GatewayForegroundService.IntentExtras(
      onionKey = onionKey,
      torRc = torRc,
      bluetoothMac = bluetoothMac,
      mqttHost = mqttHost!!,
      mqttPort = mqttPort,
      mqttUsername = mqttUsername,
      mqttPassword = mqttPassword,
      mqttTopic = mqttTopic!!,
    )

  companion object {
    const val SINGLETON_ID = 0
  }
}