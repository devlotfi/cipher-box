package com.github.devlotfi.cipherbox.gateway.data.appdatabase

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.LoginData
import com.github.devlotfi.cipherbox.shared.global.JsonInstances

class AppDatabaseConverters {
  @ColumnTypeConverter
  fun fromLoginData(value: LoginData): String =
    JsonInstances.jsonInstance.encodeToString(value)

  @ColumnTypeConverter
  fun toLoginData(value: String): LoginData =
    JsonInstances.jsonInstance.decodeFromString(value)
}