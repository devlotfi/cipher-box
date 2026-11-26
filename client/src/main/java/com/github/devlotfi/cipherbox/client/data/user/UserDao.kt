package com.github.devlotfi.cipherbox.client.data.user

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(userEntity: UserEntity)

  @Query("SELECT * FROM UserEntity WHERE rowId = ${UserEntity.SINGLETON_ID}")
  suspend fun get(): UserEntity?

  @Query("SELECT * FROM UserEntity WHERE rowId = ${UserEntity.SINGLETON_ID}")
  fun observe(): Flow<UserEntity?>

  @Query("DELETE FROM UserEntity")
  suspend fun clear()
}