package com.github.devlotfi.cipherbox.shared.data

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.shared.data.logindata.LoginDataEntity
import com.github.devlotfi.cipherbox.shared.global.JsonInstances

class LoginDbConverters {
  @ColumnTypeConverter
  fun fromAesGcmData(value: LoginDataEntity.AesGcmData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toAesGcmData(value: String): LoginDataEntity.AesGcmData =
    JsonInstances.jsonInstance.decodeFromString(value)

  @ColumnTypeConverter
  fun fromArgon2IdData(value: LoginDataEntity.Argon2IdData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toArgon2IdData(value: String): LoginDataEntity.Argon2IdData =
    JsonInstances.jsonInstance.decodeFromString(value)
}