package com.github.devlotfi.cipherbox.gateway.data.appdatabase

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.AppDataEntity
import com.github.devlotfi.cipherbox.shared.global.JsonInstances

class AppDatabaseConverters {
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