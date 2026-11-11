package com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(settingsEntity: SettingsEntity)

  @Query("SELECT * FROM SettingsEntity WHERE rowId = ${SettingsEntity.SINGLETON_ID}")
  suspend fun get(): SettingsEntity?

  @Query("SELECT * FROM SettingsEntity WHERE rowId = ${SettingsEntity.SINGLETON_ID}")
  fun observe(): Flow<SettingsEntity?>

  @Query("DELETE FROM SettingsEntity")
  suspend fun clear()
}