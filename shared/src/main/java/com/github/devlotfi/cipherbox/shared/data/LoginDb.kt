package com.github.devlotfi.cipherbox.shared.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.shared.data.appdata.AppDataDao
import com.github.devlotfi.cipherbox.shared.data.appdata.AppDataEntity

@Database(
  version = 1,
  exportSchema = false,
  entities = [
    AppDataEntity::class
  ],
)
@ColumnTypeConverters(LoginDbConverters::class)
abstract class LoginDb : RoomDatabase() {
  abstract fun appDataDao(): AppDataDao
}