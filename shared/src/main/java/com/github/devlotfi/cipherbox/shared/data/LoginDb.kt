package com.github.devlotfi.cipherbox.shared.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.shared.data.logindata.LoginDataDao
import com.github.devlotfi.cipherbox.shared.data.logindata.LoginDataEntity

@Database(
  version = 1,
  exportSchema = false,
  entities = [
    LoginDataEntity::class
  ],
)
@ColumnTypeConverters(LoginDbConverters::class)
abstract class LoginDb : RoomDatabase() {
  abstract fun loginDataDao(): LoginDataDao
}