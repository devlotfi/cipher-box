package com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SettingsEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val clientMode: ClientMode = ClientMode.SAME_DEVICE,
  val serverPort: Int = 8080,
  val torSocksProxyPort: Int = 9050,
  val bluetoothClientMac: String? = null
) {
  companion object {
    const val SINGLETON_ID = 0
  }

  enum class ClientMode {
    SAME_DEVICE,
    BLUETOOTH
  }
}