package com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SettingsEntity(
  @PrimaryKey
  val rowId: Int = SINGLETON_ID,
  val port: Int,
) {
  companion object {
    const val SINGLETON_ID = 0
  }
}