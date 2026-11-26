package com.github.devlotfi.cipherbox.client.data.contact

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface ContactDao {
  @Insert
  suspend fun insert(contactEntity: ContactEntity)

  @Update
  suspend fun update(contactEntity: ContactEntity)

  @Delete
  suspend fun delete(contactEntity: ContactEntity)

  @Query("SELECT * FROM ContactEntity WHERE id = :id")
  suspend fun getById(id: String): ContactEntity?

  @Query("SELECT * FROM ContactEntity ORDER BY id")
  suspend fun getAll(): List<ContactEntity>
}