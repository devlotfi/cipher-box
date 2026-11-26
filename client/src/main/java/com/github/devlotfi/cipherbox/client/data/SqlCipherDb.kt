package com.github.devlotfi.cipherbox.client.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
  version = 1,
  exportSchema = false,
  entities = [],
)
@ColumnTypeConverters(SqlCipherDbConverters::class)
abstract class SqlCipherDb : RoomDatabase() {

}