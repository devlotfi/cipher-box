package com.github.devlotfi.cipherbox.shared.data

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.shared.data.appdata.AppDataEntity
import com.github.devlotfi.cipherbox.shared.global.JsonInstances

class LoginDbConverters {
  @ColumnTypeConverter
  fun fromAesGcmData(value: AppDataEntity.AesGcmData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toAesGcmData(value: String): AppDataEntity.AesGcmData =
    JsonInstances.jsonInstance.decodeFromString(value)

  @ColumnTypeConverter
  fun fromArgon2IdData(value: AppDataEntity.Argon2IdData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toArgon2IdData(value: String): AppDataEntity.Argon2IdData =
    JsonInstances.jsonInstance.decodeFromString(value)
}