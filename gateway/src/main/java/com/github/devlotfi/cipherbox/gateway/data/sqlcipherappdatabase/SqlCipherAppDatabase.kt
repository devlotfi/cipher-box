package com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings.SettingsDao
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings.SettingsEntity

@Database(
  version = 1,
  entities = [
    SettingsEntity::class
  ],
)
@ColumnTypeConverters(SqlCipherAppDatabaseConverters::class)
abstract class SqlCipherAppDatabase : RoomDatabase() {
  abstract fun settingsDao(): SettingsDao
}