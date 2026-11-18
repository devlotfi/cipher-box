package com.github.devlotfi.cipherbox.gateway.data

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreManager

class SqlCipherDbConverters {
  @ColumnTypeConverter
  fun fromAesGcmEncryptedData(value: KeyStoreManager.AesGcmEncryptedData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toAesGcmEncryptedData(value: String): KeyStoreManager.AesGcmEncryptedData =
    JsonInstances.jsonInstance.decodeFromString(value)
}
