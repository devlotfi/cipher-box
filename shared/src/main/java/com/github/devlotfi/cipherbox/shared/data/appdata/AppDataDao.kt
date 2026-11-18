package com.github.devlotfi.cipherbox.shared.data.appdata

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDataDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(appDataEntity: AppDataEntity)

  @Query("SELECT * FROM AppDataEntity WHERE rowId = ${AppDataEntity.SINGLETON_ID}")
  suspend fun get(): AppDataEntity?

  @Query("SELECT * FROM AppDataEntity WHERE rowId = ${AppDataEntity.SINGLETON_ID}")
  fun observe(): Flow<AppDataEntity?>

  @Query("DELETE FROM AppDataEntity")
  suspend fun clear()
}