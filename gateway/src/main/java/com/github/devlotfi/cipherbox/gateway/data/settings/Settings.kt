package com.github.devlotfi.cipherbox.gateway.data.settings

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SettingsEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val serverPort: Int = 8080,
  val torSocksProxyPort: Int = 9050,
  val bluetoothClientMac: String = "AA:BB:CC:DD:EE:01"
) {
  companion object {
    const val SINGLETON_ID = 0
  }
}