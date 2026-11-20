package com.github.devlotfi.cipherbox.shared.data.logindata

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginDataDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(loginDataEntity: LoginDataEntity)

  @Query("SELECT * FROM LoginDataEntity WHERE rowId = ${LoginDataEntity.SINGLETON_ID}")
  suspend fun get(): LoginDataEntity?

  @Query("SELECT * FROM LoginDataEntity WHERE rowId = ${LoginDataEntity.SINGLETON_ID}")
  fun observe(): Flow<LoginDataEntity?>

  @Query("DELETE FROM LoginDataEntity")
  suspend fun clear()
}