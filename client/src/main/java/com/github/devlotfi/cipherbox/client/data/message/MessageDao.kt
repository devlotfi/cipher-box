package com.github.devlotfi.cipherbox.client.data.message

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface MessageDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(messageEntity: MessageEntity)

  @Update
  suspend fun update(messageEntity: MessageEntity)

  @Delete
  suspend fun delete(messageEntity: MessageEntity)

  @Query("SELECT * FROM MessageEntity WHERE id = :id")
  suspend fun getById(id: Int): MessageEntity?

  @Query("SELECT * FROM MessageEntity ORDER BY id")
  suspend fun getAll(): List<MessageEntity>
}