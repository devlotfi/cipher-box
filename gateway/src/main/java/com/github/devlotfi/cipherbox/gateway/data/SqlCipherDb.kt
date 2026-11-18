package com.github.devlotfi.cipherbox.gateway.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsDao
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity

@Database(
  version = 1,
  exportSchema = false,
  entities = [
    SettingsEntity::class
  ],
)
@ColumnTypeConverters(SqlCipherDbConverters::class)
abstract class SqlCipherDb : RoomDatabase() {
  abstract fun settingsDao(): SettingsDao
}