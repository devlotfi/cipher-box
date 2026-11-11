package com.github.devlotfi.cipherbox.gateway.data.appdatabase

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.AppDataDao
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.AppDataEntity

@Database(
  version = 1,
  entities = [
    AppDataEntity::class
  ],
)
@ColumnTypeConverters(AppDatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appDataDao(): AppDataDao
}